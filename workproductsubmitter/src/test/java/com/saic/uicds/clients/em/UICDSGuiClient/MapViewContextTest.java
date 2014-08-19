package com.saic.prodigal.flow;

import java.io.File;
import java.io.IOException;

import net.opengis.context.GeneralType;
import net.opengis.context.LayerListType;
import net.opengis.context.LayerType;
import net.opengis.context.ViewContextDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;

public class MapViewContextTest {

    @Test
    public void testMapViewContext() throws XmlException, IOException {

        String mapFilename = "src/test/resources/map.xml";

        // XmlObject o = XmlObject.Factory.parse(new File(mapFilename));

        ViewContextDocument context = ViewContextDocument.Factory.parse(new File(mapFilename));

        GeneralType general = context.getViewContext().getGeneral();

        LayerListType layerList = context.getViewContext().getLayerList();
        int numOfLayer = layerList.sizeOfLayerArray();
        LayerType layer = layerList.addNewLayer();
        layer.addNewSRS();
        layer.setName("Daniel's Layer");
        numOfLayer = layerList.sizeOfLayerArray();
        String newContext = context.toString();
    }
}
