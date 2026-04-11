package com.example.pathsandbox.io;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OsmParser {

    public static class OsmNode {
        private final long id;
        private final double lat;
        private final double lon;

        public OsmNode(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }

        public long getId() { return id; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }

    public static class OsmGraph {
        private final Map<Long, OsmNode> nodes;
        private final Map<Long, List<Long>> adjacencyList;

        public OsmGraph(Map<Long, OsmNode> nodes, Map<Long, List<Long>> adjacencyList) {
            this.nodes = nodes;
            this.adjacencyList = adjacencyList;
        }

        public Map<Long, OsmNode> getNodes() { return nodes; }
        public Map<Long, List<Long>> getAdjacencyList() { return adjacencyList; }

        public OsmNode findNearestNode(double targetLat, double targetLon) {
            OsmNode nearest = null;
            double minDistance = Double.MAX_VALUE;

            for (OsmNode node : nodes.values()) {
                // Ignore nodes that are completely disconnected or not on any path
                List<Long> edges = adjacencyList.get(node.getId());
                if (edges == null || edges.isEmpty()) continue;

                double dist = Math.pow(node.getLat() - targetLat, 2) + Math.pow(node.getLon() - targetLon, 2);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = node;
                }
            }

            return nearest;
        }
    }

    public static OsmGraph parse(Path xmlPath) throws Exception {
        Map<Long, OsmNode> nodes = new HashMap<>();
        Map<Long, List<Long>> adjacencyList = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (InputStream is = Files.newInputStream(xmlPath)) {
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            // 1. Extract all Nodes
            NodeList nList = doc.getElementsByTagName("node");
            for (int i = 0; i < nList.getLength(); i++) {
                Element nNode = (Element) nList.item(i);
                long id = Long.parseLong(nNode.getAttribute("id"));
                double lat = Double.parseDouble(nNode.getAttribute("lat"));
                double lon = Double.parseDouble(nNode.getAttribute("lon"));
                
                nodes.put(id, new OsmNode(id, lat, lon));
                adjacencyList.put(id, new ArrayList<>());
            }

            // 2. Extract Ways (Paths interconnecting Nodes)
            NodeList wList = doc.getElementsByTagName("way");
            for (int i = 0; i < wList.getLength(); i++) {
                Element wNode = (Element) wList.item(i);
                
                // Only parse actual traversable paths (e.g. highways/roads)
                boolean isHighway = false;
                NodeList tags = wNode.getElementsByTagName("tag");
                for (int t = 0; t < tags.getLength(); t++) {
                    Element tag = (Element) tags.item(t);
                    if ("highway".equals(tag.getAttribute("k"))) {
                        isHighway = true;
                        break;
                    }
                }
                
                if (!isHighway) {
                    continue; // Skip building outlines, water, etc.
                }

                NodeList ndRefs = wNode.getElementsByTagName("nd");
                
                List<Long> wayNodes = new ArrayList<>();
                for (int j = 0; j < ndRefs.getLength(); j++) {
                    Element nd = (Element) ndRefs.item(j);
                    wayNodes.add(Long.parseLong(nd.getAttribute("ref")));
                }

                // Connect adjacent nodes in the way 
                // Assumes bidirectional street for simplicity in pure topological extraction
                for (int j = 0; j < wayNodes.size() - 1; j++) {
                    long u = wayNodes.get(j);
                    long v = wayNodes.get(j + 1);

                    // Ensure both nodes exist inside this downloaded tile bounds
                    if (adjacencyList.containsKey(u) && adjacencyList.containsKey(v)) {
                        // Avoid duplicates
                        if (!adjacencyList.get(u).contains(v)) {
                            adjacencyList.get(u).add(v);
                        }
                        if (!adjacencyList.get(v).contains(u)) {
                            adjacencyList.get(v).add(u);
                        }
                    }
                }
            }
        }

        return new OsmGraph(nodes, adjacencyList);
    }
}
