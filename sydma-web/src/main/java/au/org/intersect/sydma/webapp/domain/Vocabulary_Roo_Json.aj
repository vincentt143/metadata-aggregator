// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package au.org.intersect.sydma.webapp.domain;

import au.org.intersect.sydma.webapp.domain.Vocabulary;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

privileged aspect Vocabulary_Roo_Json {
    
    public String Vocabulary.toJson() {
        return new JSONSerializer().exclude("*.class").serialize(this);
    }
    
    public static Vocabulary Vocabulary.fromJsonToVocabulary(String json) {
        return new JSONDeserializer<Vocabulary>().use(null, Vocabulary.class).deserialize(json);
    }
    
    public static String Vocabulary.toJsonArray(Collection<Vocabulary> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }
    
    public static Collection<Vocabulary> Vocabulary.fromJsonArrayToVocabularys(String json) {
        return new JSONDeserializer<List<Vocabulary>>().use(null, ArrayList.class).use("values", Vocabulary.class).deserialize(json);
    }
    
}
