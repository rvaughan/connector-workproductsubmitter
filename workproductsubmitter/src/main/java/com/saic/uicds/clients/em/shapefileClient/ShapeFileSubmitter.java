package com.saic.uicds.clients.em.shapefileClient;

import gov.ucore.ucore.x20.DigestMetadataType;
import gov.ucore.ucore.x20.DigestType;
import gov.ucore.ucore.x20.EntityLocationExtendedRelationshipType;
import gov.ucore.ucore.x20.EntityRefType;
import gov.ucore.ucore.x20.EventType;
import gov.ucore.ucore.x20.GeoLocationType;
import gov.ucore.ucore.x20.LocationRefType;
import gov.ucore.ucore.x20.LocationType;
import gov.ucore.ucore.x20.PointType;
import gov.ucore.ucore.x20.StringType;
import gov.ucore.ucore.x20.ThingType;
import gov.ucore.ucore.x20.WhatType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.opengis.context.LayerListType;
import net.opengis.context.LayerType;
import net.opengis.context.OnlineResourceType;
import net.opengis.context.ServerType;
import net.opengis.context.ServiceType;
import net.opengis.context.ViewContextDocument;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uicds.coreConfig.CoreConfigListType;
import org.uicds.coreConfig.CoreConfigType;
import org.uicds.directoryService.GetCoreListRequestDocument;
import org.uicds.directoryService.GetCoreListRequestType;
import org.uicds.directoryService.GetCoreListResponseDocument;
import org.uicds.directoryService.GetCoreListResponseType;
import org.uicds.directoryService.GetExternalDataSourceListRequestDocument;
import org.uicds.directoryService.GetExternalDataSourceListRequestType;
import org.uicds.directoryService.GetExternalDataSourceListResponseDocument;
import org.uicds.directoryService.GetExternalDataSourceListResponseType;
import org.uicds.directoryService.RegisterExternalDataSourceRequestDocument;
import org.uicds.directoryService.RegisterExternalDataSourceRequestType;
import org.uicds.directoryService.UnregisterExternalDataSourceRequestDocument;
import org.uicds.directoryService.UnregisterExternalDataSourceRequestType;
import org.uicds.externalDataSourceConfig.ExternalDataSourceConfigListType;
import org.uicds.externalDataSourceConfig.ExternalDataSourceConfigType;
import org.uicds.incident.IncidentDocument;
import org.uicds.incidentManagementService.GetIncidentListRequestDocument;
import org.uicds.incidentManagementService.GetIncidentListResponseDocument;
import org.uicds.mapService.GetMapRequestDocument.GetMapRequest;
import org.uicds.mapService.GetMapResponseDocument;
import org.uicds.mapService.GetMapsRequestDocument;
import org.uicds.mapService.GetMapsRequestDocument.GetMapsRequest;
import org.uicds.mapService.GetMapsResponseDocument;
import org.uicds.mapService.GetMapsResponseDocument.GetMapsResponse;
import org.uicds.mapService.SubmitLayerRequestDocument;
import org.uicds.mapService.SubmitLayerResponseDocument;
import org.uicds.mapService.SubmitShapefileRequestDocument;
import org.uicds.mapService.UpdateMapRequestDocument;
import org.uicds.mapService.SubmitLayerRequestDocument.SubmitLayerRequest;
import org.uicds.mapService.SubmitLayerResponseDocument.SubmitLayerResponse;
import org.uicds.mapService.SubmitShapefileRequestDocument.SubmitShapefileRequest;
import org.uicds.mapService.SubmitShapefileResponseDocument;
import org.uicds.mapService.SubmitShapefileResponseDocument.SubmitShapefileResponse;
import org.uicds.mapService.UpdateMapRequestDocument.UpdateMapRequest;
import org.uicds.mapService.UpdateMapResponseDocument;
import org.uicds.mapService.UpdateMapResponseDocument.UpdateMapResponse;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupRequestDocument;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupRequestDocument.AssociateWorkProductToInterestGroupRequest;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupResponseDocument;
import org.uicds.workProductService.AssociateWorkProductToInterestGroupResponseDocument.AssociateWorkProductToInterestGroupResponse;
import org.uicds.workProductService.PublishProductRequestDocument;
import org.uicds.workProductService.PublishProductRequestDocument.PublishProductRequest;
import org.uicds.workProductService.PublishProductResponseDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import x0.messageStructure1.PackageMetadataType;
import x0.messageStructure1.StructuredPayloadType;

import com.saic.precis.x2009.x06.base.IdentificationType;
import com.saic.precis.x2009.x06.base.IdentifierType;
import com.saic.precis.x2009.x06.base.PropertiesType;
import com.saic.precis.x2009.x06.base.ProcessingStateType.Enum;
import com.saic.precis.x2009.x06.payloads.binary.BinaryContentDocument;
import com.saic.precis.x2009.x06.payloads.binary.BinaryContentType;
import com.saic.precis.x2009.x06.payloads.link.LinkContentDocument;
import com.saic.precis.x2009.x06.payloads.link.LinkContentType;
import com.saic.precis.x2009.x06.structures.WorkProductDocument.WorkProduct;
import com.saic.uicds.clients.em.async.UicdsCore;
import com.saic.uicds.clients.em.async.UicdsIncident;
import com.saic.uicds.clients.em.async.UicdsIncidentManager;
import com.saic.uicds.clients.em.async.UicdsWorkProduct;
import com.saic.uicds.clients.util.Common;
import com.saic.uicds.clients.util.WebServiceClient;

public class ShapeFileSubmitter {
    private static String INCIDENT_EXAMPLE_FILE = "src/test/resources/workproduct/IncidentSample.xml";

    Logger log = LoggerFactory.getLogger(this.getClass());

    private WebServiceClient webServiceClient;
    private UicdsCore uicdsCore;
    private UicdsIncidentManager uicdsIncidentManager;

    private String profileID;
    private String uicdsID;

    private WorkProduct workProduct;

    private HashMap<String, UicdsIncident> incidents;

    // private HashMap<String, UicdsWorkProduct> uicdsWPMap;
   

    public void setUicdsCore(UicdsCore core) {

        uicdsCore = core;
    }

    public void setWebServiceClient(WebServiceClient client) {

        webServiceClient = client;
    }

    public void setProfileID(String id) {

        profileID = id;
    }

    public void setUicdsID(String id) {

        uicdsID = id;
    }

    public ShapeFileSubmitter() {

        System.out.println("in ShapeFileSubmitter()");
    }

    public boolean init(String[] args) {

        boolean returnVal = false;
        String shapefileOnly = "";

        System.out.println("in init()");

        // set the uri from the system property
        String targetUri = System.getProperty("target.uri");
        if (targetUri != null) {
            webServiceClient.setURI(targetUri);
        }

        // evaluate command line arguments
        for (int i = 0; i < args.length; i++) {
            // if there's a -u switch then there should be a target uri
            System.out.println("args[" + i + "]=" + args[i].toString());
            if (args[i].equals("-u")) {
                i++;
                if (args[i] == null) {
                    System.out.println("Switch -u must be followed with a target URI");
                    return false;
                }
                webServiceClient.setURI(args[i]);
                System.out.println("set URI: " + args[i]);
            } else if (args[i].equals("-p")) {
                i++;
                profileID = args[i];
            } else if (args[i].equals("-s")) {
                i++;
                shapefileOnly = args[i];
            } else if (args[i].equals("-i")) {
                i++;
                uicdsID = args[i];
            } else {
                usage();
            }
        }
        // Make sure we get a UICDS ID and resource profile ID
        if (profileID == null || uicdsID == null) {
            usage();
            System.exit(0);
        }
        System.out.println("targetUri=" + webServiceClient.getURI() + " profile: " + profileID
            + " UICDS ID: " + uicdsCore.getApplicationID());
        // Initialize a manager for UICDS incidents (Set before
        // initializeUicdsCore so that it
        // can register as a listener for work products because initializing the
        // core will
        // create local incidents for those already on the core.
        uicdsIncidentManager = new UicdsIncidentManager();
        uicdsIncidentManager.setUicdsCore(uicdsCore);

        // Initialize the connection to the core
        initializeUicdsCore();

        // process notifications to clear out any notifications left over
        processNotifications(2, 1000);

        // Show the current state of the client
        printStartingState(uicdsIncidentManager);

        if (shapefileOnly.equalsIgnoreCase("true")) {
            returnVal = true;
        }

        return returnVal;
    }

    private static void usage() {

        System.out.println("");
        System.out.println("This is the UICDS GUI Example Client.");
        System.out.println("Execution of this client depends on a functioning UICDS server. The default is http://localhost/uicds/core/ws/services");
        System.out.println("To verify that a UICDS server is accessible, use a browser to navigate to http://localhost/uicds/core/ws/services/ResourceProfileService.wsdl\"");
        System.out.println("");
        System.out.println("Usage: java -jar ShapefileClient.jar [-u <Server URI>] -p <Resource-Profile-ID> -i <UICDS-ID> -s <Shapefile Only?(boolean)>");
        System.out.println("");
        System.out.println("");
    }

    private void initializeUicdsCore() {

        // The UicdsCore object is created by the Spring Framework as defined in
        // async-context.xml
        // Now set the application specific data for the UicdsCore object

        // Set the UICDS identifier for the application
        // uicdsCore.setApplicationID(uicdsID);

        // Set the site local identifier for this application
    	System.out.println("*****&&&***LOCALE Id"+this.getClass().getName());
        uicdsCore.setLocalID(this.getClass().getName());

        // Set the application profile to use for the connection to the core
        // uicdsCore.setApplicationProfileID(profileID);

        // Set the Web Service Client that will handle web service invocations
        uicdsCore.setWebServiceClient(webServiceClient);

        // Initialize the connection to the core
        if (!uicdsCore.initialize()) {
            log.error("Initialization failed.  Maybe profile does not exist?");
            return;
        }
    }

    private void processNotifications(int count, int milliSeconds) {

        int iterations = -1;
        while (iterations++ < count) {
            processNotifications(milliSeconds);
        }
    }

    private void processNotifications(int milliSeconds) {

        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            log.error("Sleep interrupted: " + e.getMessage());
        }
        log.info("Processing Notifications ");
        uicdsCore.processNotifications();
    }

    private void printStartingState(UicdsIncidentManager uicdsIncidentManager) {

        log.info("Starting State:");
        HashMap<String, UicdsIncident> incidents = uicdsIncidentManager.getIncidents();
        for (String incidentID : incidents.keySet()) {
            log.info("   Incident ID:   " + incidents.get(incidentID).getIncidentID());
            log.info("   Incident Name: " + incidents.get(incidentID).getName());
            log.info("");
        }
    }

    // look for the sample xml file in a relative location
    private static IncidentDocument getIncidentSample() {

        String incidentExampleFile2 = "../" + INCIDENT_EXAMPLE_FILE;
        try {
            InputStream in = new FileInputStream(INCIDENT_EXAMPLE_FILE);
            IncidentDocument incidentDoc = IncidentDocument.Factory.parse(in);
            return incidentDoc;
        } catch (FileNotFoundException e1) {
            try {
                // try again in case this is being executed from the target
                // directory
                InputStream in = new FileInputStream(incidentExampleFile2);
                IncidentDocument incidentDoc = IncidentDocument.Factory.parse(in);
                return incidentDoc;
            } catch (FileNotFoundException e2) {
                System.err.println("File not found as either of the following paths:");
                System.err.println(INCIDENT_EXAMPLE_FILE);
                System.err.println(incidentExampleFile2);
            } catch (XmlException e2) {
                System.err.println("error parsing files " + " " + e2.getMessage());
            } catch (IOException e2) {
                System.err.println("File IO exception: " + incidentExampleFile2 + " "
                    + e2.getMessage());
            }
        } catch (XmlException e1) {
            System.err.println("error parsing files " + " " + e1.getMessage());
        } catch (IOException e1) {
            System.err.println("File IO exception: " + INCIDENT_EXAMPLE_FILE + " "
                + e1.getMessage());
        }
        return null;
    }

    public void createIncident(String incidentID, File file) {

        System.out.println("In createIncident():incidentID=" + incidentID);
        System.out.println("file contents=" + file.toString());

        // Create an incident (gets all the associated work products)
        log.info("Creating Incident");
        UicdsIncident uicdsIncident = new UicdsIncident();
        uicdsIncident.setUicdsCore(uicdsCore);

        // Create an IncidentDocument to describe the incident
        IncidentDocument incidentDoc = getIncidentSample();
        if (incidentDoc == null) {
            return;
        }

        // Create the incident on the core
        uicdsIncident.createOnCore(incidentDoc.getIncident());

        // Add it to the incident manager
        uicdsIncidentManager.addIncident(uicdsIncident);

        // process notifications to see the incident created
        processNotifications(5, 1000);
    }

    public String[][] getListOfIncidents() {

        System.out.println("in getListOfIncidents()");

        String dataValues[][];
        incidents = uicdsIncidentManager.getIncidents();
        int size = 0;
        size = incidents.size();
        dataValues = new String[size][2];

        System.out.println("keyset=" + incidents.keySet().toString());

        int i = 0;
        for (String incidentID : incidents.keySet()) {
            log.info("   Incident ID:   " + incidents.get(incidentID).getIncidentID());
            log.info("   Incident Name: " + incidents.get(incidentID).getName());

            // UICDSIncidentType incident = null;
            // incident = incidents.get(incidentID).getIncidentDocument();

            log.info("");

            dataValues[i][0] = incidents.get(incidentID).getIncidentID();
            dataValues[i][1] = incidents.get(incidentID).getName();

            i++;

            // get WPs for each incident
            // incidents.get(incidentID).getAssociatedWorkProducts();
            // uicdsWPMap = incidents.get(incidentID).getWorkProductMap();
            // int wpSize=0;
            // wpSize=uicdsWPMap.size();
            // for (String wpID: uicdsWPMap.keySet()) {
            // log.info(uicdsWPMap.get(wpID).toString());
            // }
        }
        return dataValues;
    }

    public String assocShpToIncidentUsingDOM(String incidentID, File file) {

        String res = "";
        System.out.println("In assocShpToIncident():incidentID=" + incidentID + ". Filename="
            + file.toString());
        try {
            log.info("the incident you want to attach to is:"
                + incidents.get(incidentID).getIncidentDocument().toString());

            InputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            byte[] bytes = out.toByteArray();
            bytes = Base64.encodeBase64(bytes);

            String ns = "http://uicds.org/MapService";
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElementNS(ns, "SubmitShapefileRequest");
            doc.appendChild(root);
            Element incId = doc.createElementNS(ns, "IncidentId");
            root.appendChild(incId);
            incId.setTextContent(incidentID);
            Element cdata = doc.createElementNS(ns, "ContentData");
            root.appendChild(cdata);
            cdata.setTextContent(new String(bytes));

            String digestNS = "http://ucore.gov/ucore/2.0";
            Element digestMetaElem = doc.createElementNS(digestNS, "DigestMetadata");
            digestMetaElem.setAttribute("derivedFrom", file.getName());
            Element digestElem = doc.createElementNS(digestNS, "Digest");
            digestElem.appendChild(digestMetaElem);
            root.appendChild(digestElem);

            SubmitShapefileRequestDocument ssfrd2 = SubmitShapefileRequestDocument.Factory.newInstance();
            log.info("SubmitShapefileRequestDocument ssfrd2=" + ssfrd2.toString());

            DigestMetadataType dmt = DigestMetadataType.Factory.newInstance();
            dmt.setDerivedFrom(file.getName());
            DigestType dt = DigestType.Factory.newInstance();
            dt.setDigestMetadata(dmt);

            log.info("DigestType=" + dt.toString());

            SubmitShapefileRequestDocument ssfrd = SubmitShapefileRequestDocument.Factory.parse(doc);
            log.info("SubmitShapefileRequestDocument=" + ssfrd.toString());

            XmlObject xmlObject = uicdsCore.marshalSendAndReceive(ssfrd);
            SubmitShapefileResponseDocument ssfres = (SubmitShapefileResponseDocument) xmlObject;
            Enum pst = ssfres.getSubmitShapefileResponse().getWorkProductPublicationResponseArray(0).getWorkProductProcessingStatus().getStatus();

            res = pst.toString();
            log.info("return document: \n" + xmlObject.xmlText());
            log.info("return status: \n" + res);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public String assocBinaryToIncident(String incidentID, File file, String filetype) {

        String res = "";
        String wpID = "";
        System.out.println("In assocBinaryToIncident():incidentID=" + incidentID + ". Filename="
            + file.toString());
        try {
            log.info("the incident you want to attach to is:"
                + incidents.get(incidentID).getIncidentDocument().toString());

            InputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            byte[] bytes = out.toByteArray();
            bytes = Base64.encodeBase64(bytes);

            BinaryContentType bct = BinaryContentType.Factory.newInstance();
            bct.setStringValue(new String(bytes));
            bct.setLabel(file.getName());

            BinaryContentDocument bcd = BinaryContentDocument.Factory.newInstance();
            bcd.setBinaryContent(bct);

            WorkProduct wp = WorkProduct.Factory.newInstance();

            StructuredPayloadType payload = wp.addNewStructuredPayload();

            payload.set(bcd);

            if (filetype.equalsIgnoreCase("Word")) {
                payload.addNewStructuredPayloadMetadata().setCommunityURI("application/msword");
            } else if (filetype.equalsIgnoreCase("PDF")) {
                payload.addNewStructuredPayloadMetadata().setCommunityURI("application/pdf");

            }
            payload.getStructuredPayloadMetadata().setCommunityVersion("1");

            PackageMetadataType pmt = wp.addNewPackageMetadata();

            // Add filename
            // pmt.setDataItemID(file.getName());
            pmt.setDataItemID("DataItemID");
            pmt.setDataItemReferenceID("DataItemReferenceID");
            // XmlString xstr = XmlString.Factory.newInstance();
            // xstr.setStringValue(file.getName());
            // pmt.setDataItemStatusAbstract(xstr);
            // XmlObject xo = XmlObject.Factory.newInstance();
            // xo.se)
            // pmt.setDataItemStatusAbstract(XmlObject.Factory.newInstance()
            // pmt.addNewDataOwnerMetadata().addNewDataOwnerIdentifierAbstract()

            PublishProductRequest ppr = PublishProductRequest.Factory.newInstance();
            ppr.setWorkProduct(wp);

            PublishProductRequestDocument pprd = PublishProductRequestDocument.Factory.newInstance();
            pprd.setPublishProductRequest(ppr);
            log.info("PublishProductRequestDocument=" + pprd.toString());

            XmlObject xmlObject = uicdsCore.marshalSendAndReceive(pprd);
            log.info(xmlObject.xmlText());
            
            //**Extra
            PublishProductRequestDocument pprqd = PublishProductRequestDocument.Factory.newInstance();
            PublishProductRequest pprq = PublishProductRequest.Factory.newInstance();
            pprq.setIncidentId("IG-123456");
            pprq.setWorkProduct(wp);
            
            PublishProductRequestDocument pprqd2 = PublishProductRequestDocument.Factory.parse(pprqd.xmlText());
            XmlObject xmlObject2 = uicdsCore.marshalSendAndReceive(pprqd2);
            
            //**End

            PublishProductResponseDocument ppresd = (PublishProductResponseDocument) xmlObject;

            wpID = ppresd.getPublishProductResponse().getWorkProductPublicationResponse().getWorkProduct().getPackageMetadata().getDataItemID();

            // Associate WP to IG request
            AssociateWorkProductToInterestGroupRequestDocument awptigr = AssociateWorkProductToInterestGroupRequestDocument.Factory.newInstance();
            AssociateWorkProductToInterestGroupRequest awptig = AssociateWorkProductToInterestGroupRequest.Factory.newInstance();

            IdentifierType it = IdentifierType.Factory.newInstance();
            it.setStringValue(incidentID);
            awptig.setIncidentID(it);

            it = IdentifierType.Factory.newInstance();
            it.setStringValue(wpID);
            awptig.setWorkProductID(it);
            awptigr.setAssociateWorkProductToInterestGroupRequest(awptig);

            xmlObject = uicdsCore.marshalSendAndReceive(awptigr);
            log.info(xmlObject.xmlText());
            AssociateWorkProductToInterestGroupResponseDocument awptigresd = (AssociateWorkProductToInterestGroupResponseDocument) xmlObject;

            log.info("return document: \n" + awptigresd.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Accepted";
    }

    public String assocShpToIncident(String incidentID, File file) {

        System.out.println("In assocShpToIncident():incidentID=" + incidentID + ". Filename="
            + file.toString());
        String res = "";
        try {
            log.info("the incident you want to attach to is:"
                + incidents.get(incidentID).getIncidentDocument().toString());

            InputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(in, out);
            byte[] bytes = out.toByteArray();
            bytes = Base64.encodeBase64(bytes);

            // DigestType dt = DigestType.Factory.newInstance();
            SubmitShapefileRequest ssfr = SubmitShapefileRequest.Factory.newInstance();
            ssfr.setIncidentId(incidentID);
            // ssfr.setDigest(dt);
            // ssfr.setContentData(new String(bytes).getBytes());
            ssfr.setContentData(bytes);

            SubmitShapefileRequestDocument ssrd = SubmitShapefileRequestDocument.Factory.newInstance();
            ssrd.setSubmitShapefileRequest(ssfr);

            XmlObject xmlObject = uicdsCore.marshalSendAndReceive(ssrd);
            SubmitShapefileResponse ssfres = (SubmitShapefileResponse) xmlObject;
            Enum pst = ssfres.getWorkProductPublicationResponseArray(0).getWorkProductProcessingStatus().getStatus();

            res = pst.toString();

            log.info("return document: \n" + xmlObject.toString());
            log.info("return status: \n" + pst.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public String assocLinkToIncident(String incidentID, String link) {

        String res = "";
        String wpID = "";
        String protoMarker = "://";
        int protoPos = 0;
        String protocol = "";
        System.out.println("In assocLinkToIncident():incidentID=" + incidentID + ". Link=" + link);
        try {
            log.info("The incident you want to attach to is:"
                + incidents.get(incidentID).getIncidentDocument().toString());

            protoPos = link.indexOf(protoMarker);
            protocol = link.substring(0, protoPos);
            log.info("protocol=" + protocol);

            LinkContentType lct = LinkContentType.Factory.newInstance();
            lct.setProtocol(protocol);
            lct.setAddress(link.substring(protoPos + 3));
            lct.setLabel(link);

            LinkContentDocument lcd = LinkContentDocument.Factory.newInstance();
            lcd.setLinkContent(lct);

            WorkProduct wp = WorkProduct.Factory.newInstance();
            StructuredPayloadType payload = wp.addNewStructuredPayload();
            payload.set(lcd);

            payload.addNewStructuredPayloadMetadata().setCommunityURI("text/html");
            payload.getStructuredPayloadMetadata().setCommunityVersion("1");

            PackageMetadataType pmt = wp.addNewPackageMetadata();
            pmt.setDataItemID("DataItemID");
            pmt.setDataItemReferenceID("DataItemReferenceID");
            // pmt.addNewDataOwnerMetadata().addNewDataOwnerIdentifierAbstract()

            DigestType digest = createDigestFromIncidentAndURL(incidentID, link);
            Common.setDigestElement(wp.addNewDigestAbstract(), digest);

            PublishProductRequest ppr = PublishProductRequest.Factory.newInstance();
            ppr.setWorkProduct(wp);
            PublishProductRequestDocument pprd = PublishProductRequestDocument.Factory.newInstance();
            pprd.setPublishProductRequest(ppr);

            XmlObject xmlObject = uicdsCore.marshalSendAndReceive(pprd);

            PublishProductResponseDocument ppresd = (PublishProductResponseDocument) xmlObject;

            wpID = ppresd.getPublishProductResponse().getWorkProductPublicationResponse().getWorkProduct().getPackageMetadata().getDataItemID();

            // Associate WorkProduct to InterestGroup request
            AssociateWorkProductToInterestGroupRequestDocument awptigr = AssociateWorkProductToInterestGroupRequestDocument.Factory.newInstance();
            AssociateWorkProductToInterestGroupRequest awptig = AssociateWorkProductToInterestGroupRequest.Factory.newInstance();

            IdentifierType it = IdentifierType.Factory.newInstance();
            it.setStringValue(incidentID);
            awptig.setIncidentID(it);

            it = IdentifierType.Factory.newInstance();
            it.setStringValue(wpID);
            awptig.setWorkProductID(it);

            awptigr.setAssociateWorkProductToInterestGroupRequest(awptig);
            xmlObject = uicdsCore.marshalSendAndReceive(awptigr);

            AssociateWorkProductToInterestGroupResponseDocument awptigresd = (AssociateWorkProductToInterestGroupResponseDocument) xmlObject;
            AssociateWorkProductToInterestGroupResponse awptigres = awptigresd.getAssociateWorkProductToInterestGroupResponse();
            log.info("return document: \n" + awptigres.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Accepted";
    }
    
    
    
    
    //Code for MapLayer document. assocMapLinkToIncident mapLinkLayerNameValueField mapLinkTitleValueField mapLinkSRSValueField mapLinkURLValueField
    
    public String assocMapLinkToIncident(String incidentID, String mapLyrNm, String mapTtl, String mapSrs, String mapUrl) {
    	
    	System.out.println("In assocMapLinkToIncident():incidentID=" + incidentID + ". LayerName=" + mapLyrNm+ ". Map TItle=" + mapTtl+ ". Map SRS=" + mapSrs+ ". MapURL=" + mapUrl);
    	String mapLayerName= "";
    	String mapLayerTitle= "";
    	String mapLayerSRS= "";
    	String mapLayerURL= "";
    	String incidentIDNo="";
    	String res ="";
    	StringBuffer sb = new StringBuffer();
    	try{
    		incidentIDNo = incidentID;
    		mapLayerName = mapLyrNm;
    		mapLayerTitle = mapTtl;
    		mapLayerSRS = mapSrs;
    		mapLayerURL = mapUrl;
    		//dsh
    		
    		SubmitLayerRequest slr = SubmitLayerRequest.Factory.newInstance();
    		    		
    		slr.setIncidentId(incidentIDNo);
    		slr.addNewLayer().addNewSRS().setStringValue(mapLayerSRS);
    		slr.getLayer().addNewServer().addNewOnlineResource().setHref(mapLayerURL);
    		slr.getLayer().setName(mapLayerName);
    		slr.getLayer().setTitle(mapLayerTitle);
    		    		
    		SubmitLayerRequestDocument slrd = SubmitLayerRequestDocument.Factory.newInstance();
    		slrd.setSubmitLayerRequest(slr);
    	
    		XmlObject xmlobject = uicdsCore.marshalSendAndReceive(slrd);
    		
    		 SubmitLayerResponseDocument slrresd = (SubmitLayerResponseDocument) xmlobject;
             SubmitLayerResponse slrres = slrresd.getSubmitLayerResponse();
             log.info("return document: \n" + slrres.toString());
             
             Enum pst = slrres.getWorkProductPublicationResponse().getWorkProductProcessingStatus().getStatus();
             res = pst.toString();
             
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    	
    	return res;
    	}
    
   
    public String addMapLinkToMap(String incidentID, String mapLyrNm, String mapTtl, String mapFormat, Integer servIndex, String mapSerTitle, String mapVersion, String mapSrs, String mapUrl) {
    	System.out.println("In addMapLinkToMap():incidentID=" + incidentID + ". LayerName=" + mapLyrNm+ ". Map TItle=" + mapTtl+ ". Map SRS=" + mapSrs+ ". MapURL=" + mapUrl);
    	String mapLayerName= "";
    	String mapLayerTitle= "";
    	String mapLayerSRS= "";
    	String mapLayerURL= "";
    	String incidentIDNo="";
    	String res ="";
    	
    	try{
    		
    		mapLayerName = mapLyrNm;
    		mapLayerTitle = mapTtl;
    		mapLayerSRS = mapSrs;
    		mapLayerURL = mapUrl;
    		incidentIDNo = incidentID;
    		
    		//Get the Map Work Product
    		GetMapsRequest gmrq = GetMapsRequest.Factory.newInstance();
    		gmrq.setIncidentId(incidentIDNo);
    		
    		GetMapsRequestDocument gmrqd = GetMapsRequestDocument.Factory.newInstance();
    		gmrqd.addNewGetMapsRequest();
    		gmrqd.getGetMapsRequest().set(gmrq);
    		
    		XmlObject xmlobject = uicdsCore.marshalSendAndReceive(gmrqd);
    		log.debug("xmlObject: " + xmlobject.toString());
    		GetMapsResponseDocument gmresd = (GetMapsResponseDocument) xmlobject;
    		GetMapsResponse gmres = gmresd.getGetMapsResponse();
    		
    		UpdateMapRequest umrq = UpdateMapRequest.Factory.newInstance();
    		UpdateMapRequestDocument umrqd = UpdateMapRequestDocument.Factory.newInstance();
    		    		
    		WorkProduct workProduct = WorkProduct.Factory.newInstance();   		
    		workProduct = gmres.getWorkProductArray(0);
    		log.debug("Work Product: "+ workProduct.toString());
    		
    		IdentificationType wpid = IdentificationType.Factory.newInstance();
    		wpid = Common.getIdentificationElement(workProduct);
    		log.debug("WP ID: " + wpid.toString());
    		
			XmlObject[] objects = workProduct.getStructuredPayloadArray(0).selectChildren(
						new QName("http://www.opengis.net/context",
								"ViewContext"));
			//ViewContextType map = ViewContextType.Factory.newInstance();
			ViewContextDocument map = ViewContextDocument.Factory.newInstance();
			if (objects.length > 0) {
			  try {
				  map = ViewContextDocument.Factory.parse(objects[0]
									.getDomNode());
			       }catch (XmlException e) {
				log.error("Error parsing Work Product to View Context Type: "
						+ e.getMessage());
			       }
			 }

    		log.debug("Map: "+ map.toString());
    		
    		//Get the layer list from the current Map WP		
    		LayerListType layerList = map.getViewContext().getLayerList();
    		
    		//Add the new layer
    		LayerType layer = LayerType.Factory.newInstance();
    		ServerType server = ServerType.Factory.newInstance();
    		OnlineResourceType onlineResource = OnlineResourceType.Factory.newInstance(); 		
    		server = layer.addNewServer();
    		if (servIndex == 0){
    		    server.setService(ServiceType.OGC_WMS);
    		}
    		    else {
    		    	server.setService(ServiceType.OGC_WFS);
    		    }
    		server.setTitle(mapSerTitle);
    		server.setVersion(mapVersion);
    		onlineResource = server.addNewOnlineResource();
    		onlineResource.setHref(mapLayerURL);
    		
    		layer = layerList.addNewLayer();      		 		
    		layer.setName(mapLayerName);
    		layer.setTitle(mapLayerTitle);
    		layer.setAbstract("<br/><b>Layer Name:</b>"+ mapLayerName +
    				          "<br/><b>Layer Title:</b>"+ mapLayerTitle +
    				          "<br/><b>Layer Format:</b>"+ mapFormat +
    				          "<br/><b>Layer Online Resource Source URL:</b>"+ mapLayerURL+"<br/>");
    		//set the 2 attributes of layer to false
    		layer.setHidden(false);
    		layer.setQueryable(false);
    		layer.addNewSRS().setStringValue(mapSrs);
    		layer.addNewFormatList().addNewFormat().setStringValue(mapFormat);
    		layer.setServer(server);   		  		
    		
    		log.debug("Map with New Layer: " + map.toString());
    		
    		//Setup updateMapRequest
    		umrq.setWorkProductIdentification(wpid);
    		umrq.setViewContext(map.getViewContext());
    		umrqd.addNewUpdateMapRequest().set(umrq);
 		
    		//Send the UpdateMapDocument to the core
    		XmlObject updateMapObject = uicdsCore.marshalSendAndReceive(umrqd);
    		UpdateMapResponseDocument umrd = (UpdateMapResponseDocument) updateMapObject;
            UpdateMapResponse umr = umrd.getUpdateMapResponse();
            log.info("return document: \n" + umr.toString());
            
            Enum pst = umr.getWorkProductPublicationResponse().getWorkProductProcessingStatus().getStatus();
            res = pst.toString();
    		
    	}catch (Exception e) {
    	    e.printStackTrace();
    	}
    	return res;
    }
    
	
    
    public String assocMapFileToIncident(String incidentID, File file) {   
   	 System.out.println("In assocMapFileToIncident():incidentID=" + incidentID + ". Filename="
   	            + file.toString());
   	        String res = "";
   	        try {
   	            log.info("the incident you want to attach to is:"
   	                + incidents.get(incidentID).getIncidentDocument().toString());

   //Get the file and parse it into a map layer
   	
   	LayerType newLayer = LayerType.Factory.newInstance();
   	newLayer = LayerType.Factory.parse(file);
   	log.debug("New Layer: " + newLayer.toString());
   	
   	
  //Get the Map Work Product
	GetMapsRequest gmrq = GetMapsRequest.Factory.newInstance();
	gmrq.setIncidentId(incidentID);
	
	GetMapsRequestDocument gmrqd = GetMapsRequestDocument.Factory.newInstance();
	gmrqd.addNewGetMapsRequest();
	gmrqd.getGetMapsRequest().set(gmrq);
	
	XmlObject xmlobject = uicdsCore.marshalSendAndReceive(gmrqd);
	log.debug("xmlObject: " + xmlobject.toString());
	GetMapsResponseDocument gmresd = (GetMapsResponseDocument) xmlobject;
	GetMapsResponse gmres = gmresd.getGetMapsResponse();
	
	UpdateMapRequest umrq = UpdateMapRequest.Factory.newInstance();
	UpdateMapRequestDocument umrqd = UpdateMapRequestDocument.Factory.newInstance();
	    		
	WorkProduct workProduct = WorkProduct.Factory.newInstance();   		
	workProduct = gmres.getWorkProductArray(0);
	log.debug("Work Product: "+ workProduct.toString());
	
	IdentificationType wpid = IdentificationType.Factory.newInstance();
	wpid = Common.getIdentificationElement(workProduct);
	log.debug("WP ID: " + wpid.toString());
	
	XmlObject[] objects = workProduct.getStructuredPayloadArray(0).selectChildren(
				new QName("http://www.opengis.net/context",
						"ViewContext"));
	//ViewContextType map = ViewContextType.Factory.newInstance();
	ViewContextDocument map = ViewContextDocument.Factory.newInstance();
	if (objects.length > 0) {
	  try {
		  map = ViewContextDocument.Factory.parse(objects[0]
							.getDomNode());
	       }catch (XmlException e) {
		log.error("Error parsing Work Product to View Context Type: "
				+ e.getMessage());
	       }
	 }

	log.debug("Map: "+ map.toString());
	
	//Get the layer list from the current Map WP		
	LayerListType layerList = map.getViewContext().getLayerList();
    
	LayerType layer = LayerType.Factory.newInstance();
	layer = layerList.addNewLayer();
	layer.setName(newLayer.getName());
	layer.setTitle(newLayer.getTitle());
   
	//Setup updateMapRequest
	umrq.setWorkProductIdentification(wpid);
	umrq.setViewContext(map.getViewContext());
	umrqd.addNewUpdateMapRequest().set(umrq);
	
	//Send the UpdateMapDocument to the core
	XmlObject updateMapObject = uicdsCore.marshalSendAndReceive(umrqd);
	UpdateMapResponseDocument umrd = (UpdateMapResponseDocument) updateMapObject;
    UpdateMapResponse umr = umrd.getUpdateMapResponse();
    log.info("return document: \n" + umr.toString());
    
    Enum pst = umr.getWorkProductPublicationResponse().getWorkProductProcessingStatus().getStatus();
    res = pst.toString();
   
} catch (Exception e) {
   e.printStackTrace();
}
return res;
}

   
   
   
    

    private DigestType createDigestFromIncidentAndURL(String incidentID, String link) {

        DigestType digest = DigestType.Factory.newInstance();
        digest.addNewDigestMetadata();

        EventType event = EventType.Factory.newInstance();
        event.setId("Entity.T1");
        StringType descriptor = event.addNewDescriptor();
        // descriptor.setStringValue("Location:  <IMG SRC=\"" + link
        // + "\" HEIGHT=\"200\" WIDTH=\"300\">Link</IMG>");
        // descriptor.setStringValue("Location:  <A HREF=\"" + link + "\">Link</a>");
        descriptor.setStringValue("<iframe src=\"" + link + "\" width=500 height=500 />");
        WhatType what = event.addNewWhat();
        what.setCode("Document");
        what.setCodespace("http://ucore.gov/ucore/2.0/codespace/");
        gov.ucore.ucore.x20.IdentifierType id = event.addNewIdentifier();
        id.setStringValue(link);
        id.setCode("ActivityName");
        id.setCodespace("http://niem.gov/niem/niem-core/2.0");

        Common.setEvent(event, digest);

        UicdsIncident incident = incidents.get(incidentID);
        if (incident != null) {
            UicdsWorkProduct incidentWP = null;
            HashMap<String, UicdsWorkProduct> wps = incident.getWorkProductMap();
            for (String wpid : wps.keySet()) {
                if (wpid.startsWith("Incident-")) {
                    incidentWP = wps.get(wpid);
                    break;
                }
            }
            if (incidentWP != null) {
                DigestType incidentDigest = Common.getDigest(incidentWP.getWorkProduct());
                if (incidentDigest != null) {
                    List<LocationType> locations = Common.getLocationElements(incidentDigest);

                    if (locations.size() > 0) {

                        for (LocationType location : locations) {

                            if (location.sizeOfGeoLocationArray() > 0) {

                                // PointType incidentPoint =
                                // Common.getPointFromLocationType(location);
                                gov.ucore.ucore.x20.CircleByCenterPointDocument incidentCircle = Common.getCircleByCenterPointFromLocationType(location);

                                if (incidentCircle != null) {
                                    // circle =
                                    // gov.ucore.ucore.x20.CircleByCenterPointDocument.Factory.parse(incidentCircle.getDomNode());
                                    PointType point = PointType.Factory.newInstance();
                                    net.opengis.gml.x32.PointType gmlPoint = net.opengis.gml.x32.PointType.Factory.newInstance();
                                    if (incidentCircle.getCircleByCenterPoint() != null) {
                                        if (incidentCircle.getCircleByCenterPoint().getCircleByCenterPoint().getPos() != null) {
                                            gmlPoint.addNewPos().setStringValue(
                                                incidentCircle.getCircleByCenterPoint().getCircleByCenterPoint().getPos().getStringValue());
                                            gmlPoint.setId("pt1");
                                            gmlPoint.setSrsName(incidentCircle.getCircleByCenterPoint().getCircleByCenterPoint().getPos().getSrsName());
                                            point.addNewPoint().set(gmlPoint);
                                        }
                                    }

                                    ThingType locatedAtAbstract = digest.addNewThingAbstract();
                                    EntityLocationExtendedRelationshipType locatedAt = EntityLocationExtendedRelationshipType.Factory.newInstance();
                                    EntityRefType entityRef = locatedAt.addNewEntityRef();
                                    List<String> entityRefList = new ArrayList<String>();
                                    entityRefList.add("Entity.T1");
                                    entityRef.setRef(entityRefList);
                                    LocationRefType locationRef = locatedAt.addNewLocationRef();
                                    List<String> locationRefList = new ArrayList<String>();
                                    locationRefList.add("Loc1");
                                    locationRef.setRef(locationRefList);

                                    ThingType locationAbstract = digest.addNewThingAbstract();
                                    LocationType linkLocation = LocationType.Factory.newInstance();
                                    linkLocation.setId("Loc1");

                                    GeoLocationType geoLocation = GeoLocationType.Factory.newInstance();
                                    GeoLocationType[] geoLocationArray = new GeoLocationType[1];
                                    geoLocationArray[0] = geoLocation;
                                    XmlObject geoLocationAbstract = geoLocation.addNewGeoLocationAbstract();

                                    Common.setGeoLocationElement(geoLocationAbstract, point);
                                    linkLocation.setGeoLocationArray(geoLocationArray);
                                    Common.setLocatedAtElement(locatedAtAbstract, locatedAt);
                                    Common.setLocationElement(locationAbstract, linkLocation);

                                    break;
                                } else {
                                    log.info("Cannot find point in geolocation");
                                }

                            } else {
                                log.info("location is not a geo location");
                            }

                        }
                    } else {
                        log.info("no locations found in incident digest");
                    }
                }
            } else {
                log.info("Incident work product is null");
            }
        } else {
            log.info("Unknown incident: " + incidentID);
        }

        return digest;
    }

    public List getDataSourceList() {

        String coreName = getCoreName();
        return getDataSourceList(coreName);
    }

    /*
     * get the data source list for the core name
     */
    public List getDataSourceList(String coreName) {

        // create the request document
        GetExternalDataSourceListRequestDocument request = GetExternalDataSourceListRequestDocument.Factory.newInstance();
        GetExternalDataSourceListRequestType req = request.addNewGetExternalDataSourceListRequest();
        req.setCoreName(coreName);

        // get the response document back
        GetExternalDataSourceListResponseDocument response = (GetExternalDataSourceListResponseDocument) webServiceClient.sendRequest(request);

        // extract data from response
        GetExternalDataSourceListResponseType resp = response.getGetExternalDataSourceListResponse();

        ArrayList<String> datasourceList = new ArrayList<String>();

        ExternalDataSourceConfigListType list = resp.getExternalDataSourceList();

        if (list != null) {
            ExternalDataSourceConfigType[] sources = list.getExternalDataSourceArray();
            for (ExternalDataSourceConfigType ds : sources) {
                String datasource = ds.getURN();
                datasourceList.add(datasource);
            }
        }

        return datasourceList;
    }

    /* 
     * register a data source
     */
    public void registerDataSource(String datasource) {

        RegisterExternalDataSourceRequestDocument request = RegisterExternalDataSourceRequestDocument.Factory.newInstance();

        RegisterExternalDataSourceRequestType req = request.addNewRegisterExternalDataSourceRequest();
        req.setURN(datasource);

        webServiceClient.sendRequest(request);
    }

    /*
     * unregister a data source
     */
    public void unregisterDataSource(String datasource) {

        UnregisterExternalDataSourceRequestDocument request = UnregisterExternalDataSourceRequestDocument.Factory.newInstance();

        UnregisterExternalDataSourceRequestType req = request.addNewUnregisterExternalDataSourceRequest();
        req.setURN(datasource);

        webServiceClient.sendRequest(request);
    }

    /*
     * get the core name from the core
     */
    private String getCoreName() {

        // create the request
        GetCoreListRequestDocument request = GetCoreListRequestDocument.Factory.newInstance();
        GetCoreListRequestType req = request.addNewGetCoreListRequest();

        // send the request and get the response
        GetCoreListResponseDocument response = (GetCoreListResponseDocument) webServiceClient.sendRequest(request);
        GetCoreListResponseType resp = response.getGetCoreListResponse();

        // get the list of cores from the response
        CoreConfigListType list = resp.getCoreList();

        // return the local core name
        String coreName = "";
        if (list != null) {

            CoreConfigType[] cores = list.getCoreArray();
            for (CoreConfigType core : cores) {
                System.out.println("core = " + core.getName());
                if (core.getLocalCore() == true) {
                    return core.getName();
                }
            }
        }

        return coreName;
    }

    /*
    public void runAsyncClientTest(String incidentID, File file) {
        if (webServiceClient==null)
            System.out.println("webServiceClient is NULL");
        else 
            System.out.println("webServiceClient is NOT NULL");
        
        System.out.println("In ShapeFileSubmitter():incidentID="+incidentID);
        System.out.println("file contents="+file.toString());

        // Create an incident (gets all the associated work products)
        log.info("Creating Incident");
        UicdsIncident uicdsIncident = new UicdsIncident();
        uicdsIncident.setUicdsCore(uicdsCore);
    
        // Create an IncidentDocument to describe the incident
        IncidentDocument incidentDoc = getIncidentSample();
        if (incidentDoc == null) {
            return;
        }
        
        // Create the incident on the core
        uicdsIncident.createOnCore(incidentDoc.getIncident());

        // Add it to the incident manager
        uicdsIncidentManager.addIncident(uicdsIncident);

        // process notifications to see the incident created
        processNotifications(5, 1000);

        // Get the current incident document
        UICDSIncidentType incidentType = uicdsIncident.getIncidentDocument();
        // Update the incident
        System.out.println("Updating incident: "
            + incidentType.getActivityIdentificationArray(0).getIdentificationIDArray(0).getStringValue());

        // Change the type of incident
        if (incidentType.sizeOfActivityCategoryTextArray() < 1) {
            incidentType.addNewActivityCategoryText();
        }

        incidentType.getActivityCategoryTextArray(0).setStringValue("CHANGED");
        String description = "DEFAULT DESCRIPTION";
        if (incidentType.sizeOfActivityDescriptionTextArray() < 1) {
            incidentType.addNewActivityDescriptionText();
        } else {
            description = incidentType.getActivityDescriptionTextArray(0).getStringValue();
        }
        incidentType.getActivityDescriptionTextArray(0).setStringValue(description + " - ADDITION");

        // Update the incident on the core
        ProcessingStatusType status = uicdsIncident.updateIncident(incidentType);

        // If the request is pending then process requests until the request is
        // accepted or rejected
        // Get the asynchronous completion token
        if (status != null && status.getStatus() == ProcessingStateType.PENDING) {
            IdentifierType incidentUpdateACT = status.getACT();
            System.out.println("Incident update is PENDING");

            // Process notifications from the core until the update request is
            // completed
            // This loop should also process other incoming notification
            // messages such
            // as updates for other work products
            while (!uicdsCore.requestCompleted(incidentUpdateACT)) {
                // Process messages from the core
                uicdsCore.processNotifications();

                // Get the status of the request we are waiting on
                status = uicdsCore.getRequestStatus(incidentUpdateACT);
            }

            // Check the final status of the request
            status = uicdsCore.getRequestStatus(incidentUpdateACT);
            if (status.getStatus() == ProcessingStateType.REJECTED) {
                log.error("UpdateIncident request was rejected: " + status.getMessage());
            }
        } else if (status == null) {
            System.err.println("Processing status for incident update was null");
        } else {
            System.out.println("Incident update was ACCEPTED");

            System.out.println("Close the incident");
            uicdsIncident.closeIncident(uicdsIncident.getIdentification());
            processNotifications(10, 1000);
        }

        // Dump all the work products for the incident
        // uicdsIncident.dumpWorkProducts();
        int i = 0;
        while (i < 30) {
            i++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Sleep interrupted: " + e.getMessage());
            }
            uicdsCore.processNotifications();
        }

        System.out.println("Archive the incident");
        uicdsIncident.archiveIncident(uicdsIncident.getIdentification());
        processNotifications(10, 1000);
        
    }
    */

}
