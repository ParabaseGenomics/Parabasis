/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parabasegenomics.parabasis.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3EncryptionClient;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.parabasegenomics.parabasis.aws.swf.SeqWorkflowClientExternal;
import com.parabasegenomics.parabasis.aws.swf.SeqWorkflowClientExternalFactory;
import com.parabasegenomics.parabasis.aws.swf.SeqWorkflowClientExternalFactoryImpl;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import static javafx.application.Platform.exit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 *
 * @author evanmauceli
 */
public class AWSUploader extends Application {

    private static AmazonS3EncryptionClient encryptionClient;
    
    private static final File credentialsFile 
        = new File(System.getProperty("user.home")+"/.aws/kms_credentials");
    
    private static final String KEY_TAG = "[kms]";
    
    final ChoiceBox assayChoice 
        = new ChoiceBox(FXCollections
            .observableArrayList("NBDxV1.1",
                                 "PDv3"));
    
    final static String SANDBOX_CHOICE = "sandbox";
    final static String PRODUCTION_CHOICE = "production";
      
    final ChoiceBox archiveChoice
        = new ChoiceBox(FXCollections.observableArrayList(
            PRODUCTION_CHOICE,
            SANDBOX_CHOICE));
    
    final static String PRODUCTION_BUCKET = "parabase.genomics.production";
    final static String RND_BUCKET = "parabase.genomics.sandbox";
    
    final static String DATE_FORMAT = "yyyy-MM-dd-HH:mm:ss";

    final static String PROFILE_NAME = "ParabaseProdutionUploader";
    
    final static String sucessfulUploadSuffix = "uploadSucessful";
    
    private final static String EQUALS = "=";
    private final static String AGGREGATE = "aggregate";
    
    /**
     * Paths on the MiSeq
     */
    final static String basePathString = "D:\\Illumina\\MiSeqOutput";
    final static String midPathString = "Data\\Intensities\\BaseCalls";
    final static String alignmentPathString = "\\Alignment";

    /**
     * Variant calls (sample specific)
     */
    final static String vcfFileSuffix = ".vcf";
  
    /**
     * Sequencing data (sample specific)
     */
    final static String fastqFileSuffix = ".fastq.gz";
    final static String bamFileSuffix = ".bam";
    final static String bamIndexFileSuffix = ".bam.bai";

    /**
     * QC reports (sample specific)
     */
    final static String reportFilenameSuffix = ".report.pdf";
    final static String hsMetricsFileSuffix = "_HsMetrics.txt";
    final static String gapsSuffix = ".gaps.csv";
    final static String enrichmentSuffix = ".enrichment_summary.csv";

    /**
     * Logs about the run (not sample-specific)
     */
    final static String analysisLogFilename = "AnalysisLog.txt";
    final static String analysisLogErrFilename = "AnalysisError.txt";
    final static String demultiplexSummaryFilename = "DemultiplexSummaryF1L1.txt";

    /**
     * Metainfo files about the run (not sample-specific)
     */
    final static String manifestFileSuffix = "manifest.txt";
    final static String runInfoFilename = "RunInfo.xml";
    final static String runParametersFilename = "runParameters.xml";
    final static String sampleSheetFilename = "SampleSheetUsed.csv";

    String kms_cmk_id;
    String alignmentIteration;
    String runName;
    String sampleId;
    String assay;
    String archive;
    String archiveBucket;
    String key;
    TextField runNameTextField;
    TextField sampleIdTextField;
    TextField runNumberTextField;
    Button btn;
    Button clearButton;
    Button finishButton;
    Button cancelButton;
    
    DoubleProperty uploadProgress;
    TransferManager transferManager; 
               
    public AWSUploader() {
        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        
        try {
            kms_cmk_id = readFromCredentialsFile();
        } catch (IOException ex) {
            Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("An Error Occurred.");
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();
         
        }
        
        KMSEncryptionMaterialsProvider materialProvider 
                = new KMSEncryptionMaterialsProvider(kms_cmk_id);
       
        encryptionClient = new AmazonS3EncryptionClient(
                new ProfileCredentialsProvider().getCredentials(), 
                materialProvider,
                new CryptoConfiguration().withKmsRegion(Regions.US_EAST_1))
            .withRegion(Region.getRegion(Regions.US_EAST_1));
        
        uploadProgress = new SimpleDoubleProperty();      
                   
        transferManager 
                = new TransferManager(encryptionClient);
      
        assayChoice.getSelectionModel().selectLast();
        assayChoice.setTooltip(new Tooltip("Select Assay"));
        
        archiveChoice.getSelectionModel().selectFirst();
        archiveChoice.setTooltip(new Tooltip("Select archive"));
        
        alignmentIteration="";
    }

    public void awsUpload() {}
    
    /**
     * Method to return the AWS KMS key ID from the credentials file.
     * @return A String with the AWS KMS key ID
     * @throws java.io.FileNotFoundException
     */
    private String readFromCredentialsFile() 
    throws FileNotFoundException, IOException {
        String key;
        BufferedReader reader 
            = new BufferedReader(new FileReader(credentialsFile));
        while (reader.ready()) {
            key = reader.readLine();
            if (key.equals(KEY_TAG)) {
                String keyLine = reader.readLine();
                key = keyLine.substring(keyLine.indexOf(EQUALS)+1);
                return key;
            }
        }
        throw new IOException("Cannot find KMS key in credentials file.");
    }
    
    @Override
    public void start(Stage primaryStage) {

        final GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(15, 15, 15, 15));
        Scene scene = new Scene(gridPane, 350, 300);

        Text sceneTitle = new Text("Welcome to Uploader!");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(sceneTitle, 0, 0, 2, 1);
                
        final ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(250);
         
        btn = new Button();
        btn.setText("Upload");
        btn.setStyle("-fx-background-color: mediumseagreen;"
                + " -fx-text-fill: white;"
                + " -fx-min-width: 40px;"
                + " -fx-background-radius: 5em");
        
         btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (runNameTextField.getText().isEmpty()) {
                    System.err.println("You must specify the run name.");
                } 
                if (sampleIdTextField.getText().isEmpty()) {
                    System.err.println("You must specify the sample ID");
                }
                
                runName = runNameTextField.getText();
                sampleId = sampleIdTextField.getText();
                assayChoice.show();
                assay = assayChoice.getValue().toString();
                
                /** 
                 * The assay name, used by the lab is "PDv3".  The abbreviated
                 *  test name, used in AWS S3 and Omicia, is "NBDxV2"
                 */ 
                if (assay.equals("PDv3")) {
                    assay = "NBDxV2";
                }
                if (runNumberTextField.getText() != null) {
                    alignmentIteration = runNumberTextField.getText();
                }
                
                archiveChoice.show();
                archiveBucket = archiveChoice.getValue().toString();
                
                try {
                    progressBar.progressProperty().unbind();
                    progressBar.progressProperty().bind(uploadProgress);
               
                    // we use the update date in the AWS key
                    DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                    Date date = new Date();
                    String dateString = dateFormat.format(date);

                    // only one bucket allowed for produciton data
                    String bucketName= PRODUCTION_BUCKET;
                    if (archiveBucket.equals(SANDBOX_CHOICE)) {
                        bucketName = RND_BUCKET;
                    }
                   
                    List<File> files = getFileList();
                    List<File> compressedFiles = compressFiles(files);
                    File compressedFileDirectory
                        = new File(getPathToCompressedFiles());
                                        
                    // AWS file key
                    key
                        = assay
                        + "/"
                        + sampleId
                        + "/"
                        + dateString
                        + "/"
                        + runName;
                    if (!alignmentIteration.isEmpty()) {
                        key += ("_" + alignmentIteration);
                    }
                    key += ("/");                     
                  
                    final MultipleFileUpload upload;
                    upload = transferManager
                            .uploadFileList(
                                    bucketName,
                                    key,
                                    compressedFileDirectory,    
                                    compressedFiles);

                    ProgressListener progressListener 
                        = new ProgressListener() {
                            @Override
                            public void progressChanged(ProgressEvent progressEvent) {
                                uploadProgress.set(
                                    upload.getProgress().getPercentTransferred()/100.);                       
                                if (upload.isDone()) {
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {                                           
                                            cleanWrapup();
                                        }
                                    });
                                    //cleanWrapup();
                                }          
                            }  
                        };

                    upload.addProgressListener(progressListener);

                 } catch (Exception ex) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("An Error Occurred.");
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();
                        //Logger.getLogger(AWSUploader.class.getName()).log(Level.SEVERE, null, ex);
                    }                             
            }
            
        });

        clearButton = new Button();
        clearButton.setText("Clear");
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event) {
               runNameTextField.clear();
               sampleIdTextField.clear();
               runNumberTextField.clear();
               uploadProgress.unbind();
               uploadProgress.setValue(0);  
               assayChoice.getSelectionModel().selectLast();
               archiveChoice.getSelectionModel().selectFirst();

           }
        });
        
        finishButton = new Button();
        finishButton.setText("Finish");
        finishButton.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent event) {
               transferManager.shutdownNow();
               exit();
           }
        });
        
        cancelButton = new Button();
        cancelButton.setCancelButton(true);
        cancelButton.setText("Cancel");
        cancelButton.setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);"
                + " -fx-text-fill: white;"
                + " -fx-min-width: 40px;"
                + " -fx-background-radius: 5em");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (archiveBucket != null && !archiveBucket.isEmpty()) {
                    transferManager.abortMultipartUploads(archiveBucket,new Date());
                }
                transferManager.shutdownNow();
                runNameTextField.clear();
                sampleIdTextField.clear();
                runNumberTextField.clear();
                exit();
            }
        });
        
        gridPane.add(btn, 1, 1);
        gridPane.add(clearButton,1,2);
        gridPane.add(finishButton, 1, 3);
        gridPane.add(cancelButton,1,6);
        gridPane.add(progressBar,0,6);
        
      
        runNameTextField = new TextField();
        runNameTextField.setPromptText("Enter run name.");
        //HBox textBox = new HBox();
        //Label textBoxLabel = new Label("Run name:");
        //textBox.getChildren().addAll(textBoxLabel, runNameTextField);

        sampleIdTextField = new TextField();
        sampleIdTextField.setPromptText("Enter sample ID.");
           
        runNumberTextField = new TextField();
        runNumberTextField.setPromptText("#");
        runNumberTextField.setMaxWidth(30);
        //HBox runNumberTextBox = new HBox();
        //Label runNumberBoxLabel = new Label("Alignment#");
        //runNumberTextBox.getChildren().addAll(runNumberBoxLabel,runNumberTextField);
        
        
        gridPane.add(runNameTextField, 0, 1);
        gridPane.add(sampleIdTextField, 0, 2);
        gridPane.add(assayChoice, 0, 3);
        gridPane.add(archiveChoice,0,4);
        gridPane.add(runNumberTextField,1,4);
             
        primaryStage.setTitle("Uploader");
        primaryStage.setScene(scene);
        primaryStage.show();
   
}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        exit();
    }

    /**
     * Define the local path to the compressed files.
     * @return Returns the full path to the directory holding the compressed
     * files to transfer to AWS.
     */
    String getPathToCompressedFiles() {
        String fullPathToAlignmentDir=alignmentPathString;
        if (!alignmentIteration.isEmpty()) {
            fullPathToAlignmentDir 
                = alignmentPathString + alignmentIteration;
        }
     
        String fullPathToCompressedDir
            = basePathString 
            + "\\"
            + runName
            + "\\"
            + midPathString
            + "\\"
            + fullPathToAlignmentDir
            + "\\Compressed";

        return fullPathToCompressedDir;
    }
    
    /**
     * Method to compress files prior to upload.
     * @param fileList
     * @return Returns the list of compressed files.
     * @throws java.io.IOException
     */
    public List<File> compressFiles(List<File> fileList) 
    throws IOException {
        List<File> compressedFiles = new ArrayList<>();

        File compressedFileDirectory
            = new File(getPathToCompressedFiles());
        
        if (!compressedFileDirectory.exists()) {
            compressedFileDirectory.mkdir();
        }
           
        for (File file : fileList) {       
            String filename = file.getName();
            if (!filename.contains(fastqFileSuffix) 
                    && !filename.contains(bamFileSuffix)) {

                File compressedFile 
                    = gzipFile(file,compressedFileDirectory);

                compressedFiles.add(compressedFile);
            } else {
                File compressedFile
                    = new File(compressedFileDirectory.getAbsolutePath()
                    + "\\"
                    + file.getName());

                Files.copy(
                    file.toPath(),
                    compressedFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

                compressedFiles.add(compressedFile);
            }
        }
      
        return compressedFiles;
    }
    
    /**
     * Method to generate the list of files to compress.
     * @return 
     * @throws java.io.IOException 
     */
    public List<File> getFileList() 
    throws IOException {
        List<File> fileList = new ArrayList<>();
        
            final String sampleFileIdentifier = sampleId + "_";
        
            String fullPathToAlignmentDir 
            = basePathString 
                + "\\"
                + runName
                + "\\"
                + midPathString
                + "\\"
                + alignmentPathString;
            if (!alignmentIteration.isEmpty()) {
                fullPathToAlignmentDir += alignmentIteration;
            }
           
        File sfs = new File(fullPathToAlignmentDir);
        File [] fileArray = sfs.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.startsWith(sampleFileIdentifier));  
            }
        });

        if (fileArray==null 
                || fileArray.length < 1) {
            throw new IOException("Cannot find files for sample " + sampleId + "."); 
        } else {
            List<File> manifestFiles 
                    = Arrays.asList(fileArray);
            for (File file : manifestFiles) {
                fileList.add(file);
            }
        }   
        
        fileList.add(new File(
            fullPathToAlignmentDir
            + "\\"
            + sampleSheetFilename));    
                     
        fileList.add(new File(
            fullPathToAlignmentDir
            + "\\"
            + demultiplexSummaryFilename));
        
        fileList.add(new File(
            fullPathToAlignmentDir
            + "\\"
            + AGGREGATE
            + reportFilenameSuffix));
        
        fileList.add(new File(
            fullPathToAlignmentDir
            + "\\"
            + AGGREGATE
            + enrichmentSuffix));

        // fastq files    
        String fullPathToBasecallsDir
            = new String(
            basePathString 
            + "\\"
            + runName
            + "\\"
            + midPathString);

        // metainfo and logs
        String fullPathToRunDir
            = new String(
            basePathString
            + "\\"
            + runName);
        fileList.add(new File(
            fullPathToRunDir
            + "\\"
            + runInfoFilename));
        fileList.add(new File(
            fullPathToRunDir
            + "\\"
            + runParametersFilename));
    
        fileList.add(new File(
            fullPathToRunDir
            + "\\"
            + analysisLogFilename));
        fileList.add(new File(
            fullPathToRunDir
            + "\\"
            + analysisLogErrFilename));

        
        File bfs = new File(fullPathToAlignmentDir);
        File [] bamFileArray = bfs.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.startsWith(sampleFileIdentifier)
                    && (name.endsWith(bamFileSuffix) 
                        || name.endsWith(bamIndexFileSuffix)));  
            }
        });

        if (bamFileArray==null 
                || bamFileArray.length < 1) {
            throw new IOException("Cannot find fastq files."); 
        } else {
            List<File> manifestFiles 
                    = Arrays.asList(bamFileArray);
            for (File file : manifestFiles) {
                fileList.add(file);
            }
        }        

        // fastq files are tricky as they are most likely to be 
        // broken into multiple files
        File fqs = new File(fullPathToBasecallsDir);
        File [] fastqFileArray = fqs.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.startsWith(sampleFileIdentifier)
                    && name.endsWith(fastqFileSuffix));  
            }
        });

        if (fastqFileArray==null 
                || fastqFileArray.length < 1) {
            throw new IOException("Cannot find fastq files."); 
        } else {
            List<File> manifestFiles 
                    = Arrays.asList(fastqFileArray);
            for (File file : manifestFiles) {
                fileList.add(file);
            }
        }        


        // manifest files are tricky as they depend loosely on the assay nanme
        File f = new File(fullPathToRunDir);
        File [] manifestFileArray = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(manifestFileSuffix);  
            }
        });

        if (manifestFileArray==null 
                || manifestFileArray.length < 1) {
            throw new IOException("Cannot find manifest files."); 
        } else {
            List<File> manifestFiles 
                    = Arrays.asList(manifestFileArray);
            for (File file : manifestFiles) {
                fileList.add(file);
            }
        }

        return fileList;
    }
    
    /**
     * Method defining what happens after a clean upload.
     */
    public void cleanWrapup() {
        
        if (archiveBucket.equals(PRODUCTION_CHOICE)) {
            ClientConfiguration config 
                = new ClientConfiguration().withSocketTimeout(70*1000);   
        
            AWSCredentials credentials 
                = new ProfileCredentialsProvider().getCredentials();

            AmazonSimpleWorkflow service
                = new AmazonSimpleWorkflowClient(credentials,config);

            service.setEndpoint("https://swf.us-east-1.amazonaws.com");
            String domain = "PushToOmiciaDomain";
   
             // run gaps report
             // but wait - this depends on the test ordered!!!!
             // but the key has the test!!
             // but we'll need to parse the key!!!
             //  
            String bamFilepath
                = PRODUCTION_BUCKET
                + "/" 
                + key
                + "/" 
                + sampleId 
                + ".bam";
            
            String bamIndexFilepath
                = PRODUCTION_BUCKET
                + "/" 
                + key
                + "/" 
                + sampleId 
                + ".bam.bai";
                    
            String vcfFilepath 
                = PRODUCTION_BUCKET
                + "/" 
                + key
                + "/"
                + sampleId
                + vcfFileSuffix
                + ".gz";
         
            PushToOmiciaWorkflowClientExternalFactory pushToOmiciaFactory
                = new PushToOmiciaWorkflowClientExternalFactoryImpl(service,domain);
        
            PushToOmiciaWorkflowClientExternal pusher
                = pushToOmiciaFactory.getClient(sampleId);
            
            //String testOrdered = parseTest(key);
            pusher.push(vcfFilepath);
            //pusher.push(vcfFilepath, testOrdered,bamFilepath, bamIndexFilepath);
       
        }
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Copy complete!");
        alert.showAndWait();
       
        String fullPathToRunDir
            = new String(
            basePathString
            + "\\"
            + runName);
        
        File doneFile = new File(
            fullPathToRunDir
            + "\\"
            + sampleId
            + "."
            + sucessfulUploadSuffix);
        
        try {
            doneFile.createNewFile();
        } catch (IOException ex) {
            Alert localWriteAlert = new Alert(AlertType.ERROR);
            localWriteAlert.setTitle("ERROR");
            localWriteAlert.setHeaderText(null);
            localWriteAlert.setContentText("Cannot create upload successful file.");
            localWriteAlert.showAndWait();
        }

        runNameTextField.clear();
        sampleIdTextField.clear();
        runNumberTextField.clear();
        uploadProgress.unbind();
        uploadProgress.setValue(0);
        transferManager.shutdownNow();
    }
    
    /**
     * Method to compress a file using gzip
     * @param in the file to compress   
     * @param outDirectory   
     * @return the compressed file
     * @throws java.io.FileNotFoundException
     */
    public File gzipFile(File in, File outDirectory) 
    throws FileNotFoundException, IOException {  
        
        if (!outDirectory.isDirectory()) {
            outDirectory.mkdir();
        }
        
        File outputGzipFile 
                = new File(outDirectory 
                    + "\\"
                    + in.getName()
                    + ".gz");
        
        FileInputStream inputStream = new FileInputStream(in);
        FileOutputStream outputStream
                = new FileOutputStream(outputGzipFile.getAbsolutePath());
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        int readByte;
        while ((readByte = inputStream.read()) != -1) {
            gzipOutputStream.write(readByte);
        }
        inputStream.close();
        gzipOutputStream.close();
        outputStream.close();

        return outputGzipFile;
    }
}
