package ch.hsr.ifs.cute.ui.commands.parameters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;


public class AddRemoveCuteNatureParameter implements IParameterValues {

    private static final Map<String, String> VALUES = new HashMap<>();

    static {
        VALUES.put("Add", "add");
        VALUES.put("Remove", "remove");
    }

    @Override
    public Map<String, String> getParameterValues() {
        return VALUES;
    }

}
