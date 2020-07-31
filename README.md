### what is this
a library

### how this
```
public class Configuration extends BasonConfiguration {
    public static final INSTANCE = new Configuration();

    public boolean isThisModEnabled;

    public Configuration() {
        super("namespace", "file");
    }
    
    public void init() {
        this.isThisModEnabled = true;
    }
}

public class Main {
    public static boolean isThisModEnabled() {
        return Configuration.INSTANCE.isThisModEnabled;
    }
}
```

### when this
when you want a small mod

### why this
if you want a small mod

### where this
jitpack

```gradle
repositories {
    . . .
    maven {
        url "https://jitpack.io"
    }
}
. . .
dependencies {
    . . .
    implementation include("com.github.user11681:bason:1.16-SNAPSHOT")
}
```
