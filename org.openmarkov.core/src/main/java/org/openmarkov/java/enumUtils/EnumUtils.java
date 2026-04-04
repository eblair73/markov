package org.openmarkov.java.enumUtils;

public class EnumUtils {
    public static String toCamelCase(Enum<?> enumConstant) {
        var name = enumConstant.name();
        StringBuilder sb = new StringBuilder();
        var shouldCapitalize = false;
        for (var character : name.toCharArray()) {
            if (character == '_') {
                shouldCapitalize = true;
                continue;
            }
            if (shouldCapitalize) {
                sb.append(Character.toUpperCase(character));
                shouldCapitalize = false;
            } else {
                sb.append(Character.toLowerCase(character));
            }
        }
        return sb.toString();
    }
    
    public static String toPascalCase(Enum<?> enumConstant) {
        var name = enumConstant.name();
        StringBuilder sb = new StringBuilder();
        var shouldCapitalize = true;
        for (var character : name.toCharArray()) {
            if (character == '_') {
                shouldCapitalize = true;
                continue;
            }
            if (shouldCapitalize) {
                sb.append(Character.toUpperCase(character));
                shouldCapitalize = false;
            } else {
                sb.append(Character.toLowerCase(character));
            }
        }
        return sb.toString();
    }
    
    public static String toTitleCase(Enum<?> enumConstant) {
        var name = enumConstant.name();
        StringBuilder sb = new StringBuilder();
        var shouldCapitalize = true;
        for (var character : name.toCharArray()) {
            if (character == '_') {
                sb.append(" ");
                shouldCapitalize = true;
                continue;
            }
            if (shouldCapitalize) {
                sb.append(Character.toUpperCase(character));
                shouldCapitalize = false;
            } else {
                sb.append(Character.toLowerCase(character));
            }
        }
        return sb.toString();
    }
    
}
