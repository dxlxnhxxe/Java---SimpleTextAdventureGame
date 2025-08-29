package edu.uob;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

public class GameActionParser
{
    public static LinkedList<GameActionNode> XMLList = new LinkedList<>();
    public static Set<String> extendedCommands = new HashSet<>();
    public static Map<String, String> extendedKeyphraseSynonyms = new HashMap<>();

    public static void parseXML(File actionsFile) {

        if (!actionsFile.exists()) {
            System.out.println("Actions file does not exist");
            return;
        }
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(actionsFile);
            doc.getDocumentElement().normalize();

            NodeList actions = doc.getElementsByTagName("action");
            for (int i = 0; i < actions.getLength(); i++) {
                Element action = (Element) actions.item(i);

                //Declare variables
                LinkedList<String> keyphrases = new LinkedList<>();
                LinkedList<String> subjects = new LinkedList<>();
                LinkedList<String> consumedEntities = new LinkedList<>();
                LinkedList<String> producedEntities = new LinkedList<>();
                String narrationText = "";

                //extract triggers and their keyphrases
                NodeList triggers = action.getElementsByTagName("triggers");
                for (int j = 0; j < triggers.getLength(); j++) {
                    Element trigger = (Element) triggers.item(j);
                    NodeList keyphraseNodes = trigger.getElementsByTagName("keyphrase");
                    for (int k = 0; k < keyphraseNodes.getLength(); k++) {
                        String phrase = keyphraseNodes.item(k).getTextContent();
                        keyphrases.add(phrase);
                        extendedCommands.add(phrase);

                        if (phrase.contains(" ")){
                            String collapsed = phrase.replaceAll("\\s+", "_");
                            extendedKeyphraseSynonyms.put(phrase, collapsed);
                        } else {
                            extendedKeyphraseSynonyms.put(phrase, phrase);
                        }
                    }
                }
                //Extract subjects entities
                NodeList subjectNodes = action.getElementsByTagName("subjects");
                for (int j = 0; j < subjectNodes.getLength(); j++) {
                    Element subject = (Element) subjectNodes.item(j);
                    NodeList subjectEntities = subject.getElementsByTagName("entity");
                    for (int k = 0; k < subjectEntities.getLength(); k++) {
                        String entity = subjectEntities.item(k).getTextContent();
                        subjects.add(entity);
                    }
                }
                //Extract consumed and entities
                NodeList consumedNodes = action.getElementsByTagName("consumed");
                for (int j = 0; j < consumedNodes.getLength(); j++) {
                    Element consumed = (Element) consumedNodes.item(j);
                    NodeList consumedEntityNodes = consumed.getElementsByTagName("entity");
                    for (int k = 0; k < consumedEntityNodes.getLength(); k++) {
                        String entity = consumedEntityNodes.item(k).getTextContent();
                        consumedEntities.add(entity);
                    }
                }
                //Extract produced and entities
                NodeList producedNodes = action.getElementsByTagName("produced");
                for (int j = 0; j < producedNodes.getLength(); j++) {
                    Element producedElement = (Element) producedNodes.item(j);
                    NodeList producedEntityNodes = producedElement.getElementsByTagName("entity");
                    for (int k = 0; k < producedEntityNodes.getLength(); k++) {
                        String entity = producedEntityNodes.item(k).getTextContent();
                        producedEntities.add(entity);
                    }
                }
                //Extract the narrative
                NodeList narration = action.getElementsByTagName("narration");
                if (narration.getLength() > 0) {
                    narrationText = narration.item(0).getTextContent();
                }
                //Add action to list
                GameActionParser.addActionToList(keyphrases, subjects, consumedEntities, producedEntities, narrationText);
            }

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private static void addActionToList(LinkedList<String> keyphrase, LinkedList<String> subject,
                                        LinkedList<String> consumed, LinkedList<String> produced, String narration) {
        GameActionNode newNode = new GameActionNode(keyphrase, subject, consumed, produced, narration);
        XMLList.add(newNode);
    }

}
