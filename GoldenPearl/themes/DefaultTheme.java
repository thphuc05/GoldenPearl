package themes;

import com.formdev.flatlaf.FlatLightLaf;

public class DefaultTheme extends FlatLightLaf {
    public static boolean setup() {
        return setup(new DefaultTheme());
    }

    @Override
    public String getName() {
        return "DefaultTheme";
    }
}
