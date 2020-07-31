package user11681.bason;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is the base class for bason configurations. Instances of its subclasses<br>
 * have their fields serialized and deserialized unless they have the {@code transient} modifier.
 */
public abstract class BasonConfiguration {
    protected transient final JsonParser parser = new JsonParser();
    protected transient final String path;
    protected transient final Gson gson;
    protected transient final File file;
    protected transient final Logger logger;

    public BasonConfiguration(final String namespace, final String path) {
        this(namespace, path, new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create());
    }

    public BasonConfiguration(final String namespace, final String path, final Gson gson) {
        this(namespace, path, LogManager.getLogger(String.format("%s/%s", namespace, path.replaceFirst("\\..*$", ""))), gson);
    }

    public BasonConfiguration(final String namespace, final String path, final Logger logger) {
        this(namespace, path, logger, new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create());
    }

    public BasonConfiguration(final String namespace, final String path, final Logger logger, final Gson gson) {
        this.path = namespace;
        this.file = new File(FabricLoader.getInstance().getConfigDir().toFile(), path + ".json");
        this.logger = logger;
        this.gson = gson;

        this.read();
    }

    public void write() {
        try {
            if (this.file.createNewFile()) {
                this.logger.info("Making a new configuration file.");

                this.init();
            } else {
                try (final FileOutputStream output = new FileOutputStream(this.file)) {
                    output.write(this.serialize().getBytes());
                } catch (final FileNotFoundException exception) {
                    this.logger.error("The file was not found despite existing; initializing default values.");

                    this.init();
                } catch (final IOException exception) {
                    this.logger.error("Unable to write to the file.");

                    this.init();
                }
            }
        } catch (final IOException exception) {
            this.logger.error("The file could not be made; initializing default values.");

            this.init();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void read() {
        try {
            if (this.file.createNewFile()) {
                this.logger.info("Generating a new file.");

                this.init();
                this.write();
            } else {
                try (final FileInputStream input = new FileInputStream(this.file)) {
                    final byte[] output = new byte[input.available()];

                    while (input.read(output) > -1) ;

                    final JsonElement json = this.parser.parse(new String(output));

                    this.deserialize(json);
                } catch (final FileNotFoundException exception) {
                    this.logger.error("The file was not found despite existing; initializing default values.");

                    this.init();
                } catch (final IOException exception) {
                    this.logger.error("File reading failed; initializing default values");

                    this.init();
                }
            }
        } catch (final IOException exception) {
            this.logger.error("The file could not be made; initializing default values.");

            this.init();
        }
    }

    protected void deserialize(final JsonElement json) {
        final BasonConfiguration that = gson.fromJson(json, this.getClass());

        for (final Field field : this.getFields()) {
            final Class<?> type = field.getType();
            field.setAccessible(true);

            try {
                if (type == boolean.class) {
                    field.setBoolean(this, field.getBoolean(that));
                } else if (type == char.class) {
                    field.setChar(this, field.getChar(that));
                } else if (type == byte.class) {
                    field.setByte(this, field.getByte(that));
                } else if (type == short.class) {
                    field.setShort(this, field.getShort(that));
                } else if (type == int.class) {
                    field.setInt(this, field.getInt(that));
                } else if (type == long.class) {
                    field.setLong(this, field.getLong(that));
                } else if (type == float.class) {
                    field.setFloat(this, field.getFloat(that));
                } else if (type == double.class) {
                    field.setDouble(this, field.getDouble(that));
                } else {
                    field.set(this, field.get(that));
                }
            } catch (final IllegalAccessException exception) {
                this.logger.error("Deserialization of field {} {} failed.", field.getType().getSimpleName(), field.getName());
            }
        }
    }

    protected String serialize() {
        return this.gson.toJson(this);
    }

    protected List<Field> getFields() {
        final List<Field> fields = new ReferenceArrayList<>();
        Class<?> klass = this.getClass();

        while (klass != BasonConfiguration.class) {
            for (final Field field : klass.getDeclaredFields()) {
                if ((field.getModifiers() & Modifier.STATIC) == 0) {
                    fields.add(field);
                }
            }

            klass = klass.getSuperclass();
        }

        return fields;
    }

    /**
     * initialize fields to default values
     */
    public abstract void init();
}
