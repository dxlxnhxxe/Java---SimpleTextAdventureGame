package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GameEntityParser {

    public static final Map<String, Locations> allLocations = new HashMap<>();
    public static final Map<String, LinkedList<String>> locationPaths = new HashMap<>();
    public static Locations startingLocation;

    public static final Map<String, GameEntity> allEntities = new HashMap<>();
    public static final Map<String, Map<String, Players>> locationWithPlayers = new HashMap<>();
    public static final Map<String, Map<String, Furnitures>> locationWithFurnitures = new HashMap<>();
    public static final Map<String, Map<String, Artefacts>> locationWithArtefacts = new HashMap<>();
    public static final Map<String, Map<String, Characters>> locationWithCharacters = new HashMap<>();

    public static final Set<String> artefactName = new HashSet<>();
    public static final Set<String> furnitureName = new HashSet<>();
    public static final Set<String> characterName = new HashSet<>();


    public static void parseEntities(File entitiesFile){
        if(!entitiesFile.exists()){
            return;
        }
        try (FileReader entitiesFileReader = new FileReader(entitiesFile)){
            Parser parser = new Parser();
            parser.parse(entitiesFileReader);
            Graph wholeDocument = parser.getGraphs().get(0);
            GameEntityParser.processGraph(wholeDocument);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void processGraph(Graph wholeDocument){
        //Iterate through outergraphs
        for (Graph outerGraph : wholeDocument.getSubgraphs()) {
            if (outerGraph.getId().getId().equals("locations")) {
                //Iterate over each cluster
                for (Graph locationCluster : outerGraph.getSubgraphs()) {
                    //Get first node
                    Node areaNode = locationCluster.getNodes(false).get(0);
                    String areaName = areaNode.getId().getId().toLowerCase();
                    String areaDescription = areaNode.getAttribute("description").toLowerCase();
                    if (locationCluster.getId().getId().contains("cluster")){
                        Locations location = new Locations(areaName, areaDescription);
                        allLocations.put(areaName, location);
                        allEntities.put(areaName, location);
                        //Assign first node to starting location
                        if (startingLocation == null){
                            startingLocation = location;
                        }
                    }

                    //Loop through each category
                    for (Graph categoryID : locationCluster.getSubgraphs()) {
                        String entityType = categoryID.getId().getId();
                        //Loop through each node under categories
                        for (Node entities : categoryID.getNodes(false)) {
                            if (entities.getAttribute("description") != null) {
                                String itemName = entities.getId().getId().toLowerCase();
                                String itemDescription = entities.getAttribute("description").toLowerCase();
                                if ("furniture".equals(entityType)) {
                                    Furnitures furnitureEntity = new Furnitures(itemName, itemDescription);
                                    Map<String, Furnitures> furnituresInLocation = locationWithFurnitures.get(areaName);
                                    if (furnituresInLocation == null) {
                                        furnituresInLocation = new HashMap<>();
                                        locationWithFurnitures.put(areaName, furnituresInLocation);
                                    }
                                    furnituresInLocation.put(itemName, furnitureEntity);
                                    furnitureName.add(itemName);
                                    allEntities.put(itemName, furnitureEntity);
                                }
                                if ("artefacts".equals(entityType)) {
                                    Artefacts  artefactEntity =  new Artefacts(itemName, itemDescription);
                                    Map<String, Artefacts> artefactsInLocation = locationWithArtefacts.get(areaName);
                                    if (artefactsInLocation == null) {
                                        artefactsInLocation = new HashMap<>();
                                        locationWithArtefacts.put(areaName, artefactsInLocation);
                                    }
                                    artefactsInLocation.put(itemName, artefactEntity);
                                    artefactName.add(itemName);
                                    allEntities.put(itemName, artefactEntity);
                                }
                                if ("characters".equals(entityType)) {
                                    Characters  characterEntity  =  new Characters(itemName, itemDescription);
                                    Map<String, Characters> charactersInLocation = locationWithCharacters.get(areaName);
                                    if (charactersInLocation == null) {
                                        charactersInLocation = new HashMap<>();
                                        locationWithCharacters.put(areaName, charactersInLocation);
                                    }
                                    charactersInLocation.put(itemName, characterEntity);
                                    characterName.add(itemName);
                                    allEntities.put(itemName, characterEntity);
                                }
                            }
                        }
                    }
                }
            }
            //Find "paths"
            if (outerGraph.getId().getId().equals("paths")) {
                GameEntityParser.parsePaths(outerGraph);

            }
        }
    }
    private static void parsePaths(Graph pathsGraph){
        //Iterate through edges
        Iterator<Edge> edgeIterator = pathsGraph.getEdges().iterator();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            //Convert to string
            String from = edge.getSource().getNode().getId().getId();
            String to = edge.getTarget().getNode().getId().getId();
            //Create directional LinkedList of string
            LinkedList<String> fromPaths = locationPaths.get(from);
            if (fromPaths == null) {
                fromPaths = new LinkedList<>();
                locationPaths.put(from, fromPaths);
            }
            if (!fromPaths.contains(to)) {
                fromPaths.add(to);
            }
        }
    }
}