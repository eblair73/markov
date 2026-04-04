package org.openmarkov.gui.configuration.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class GsonAdapters {
    
    public static final class FileAdapter extends TypeAdapter<File> {
        
        public FileAdapter() {
        }
        
        @Override public void write(JsonWriter out, File value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.getPath());
            }
        }
        
        @Override public @Nullable File read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return new File(in.nextString());
        }
    }
    
}
