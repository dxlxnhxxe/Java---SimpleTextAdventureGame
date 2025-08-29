package edu.uob;

import java.util.List;
import java.util.LinkedList;

public class GameActionNode {
    LinkedList<String> keyphrases;
    LinkedList<String> subjects;
    LinkedList<String> consumed;
    LinkedList<String> produced;
    String narration;

    public GameActionNode(LinkedList<String> keyphrases, LinkedList<String> subjects,
                          LinkedList<String> consumed, LinkedList<String> produced, String narration) {
        this.keyphrases = keyphrases;
        this.subjects = subjects;
        this.consumed = consumed;
        this.produced = produced;
        this.narration = narration;
    }

    public boolean matchesKeyphrase (String input){
        for (String keyphrase : keyphrases) {
            if (input.contains(keyphrase)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getKeyphrases(){
        return keyphrases;
    }

    public List<String> getSubjects(){
        return subjects;
    }

    public List<String> getConsumed(){
        return consumed;
    }

    public List<String> getProduced(){
        return produced;
    }

    public String narration() {
        return narration;
    }

}
