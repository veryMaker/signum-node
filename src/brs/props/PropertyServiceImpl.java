package brs.props;

import brs.Burst;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class PropertyServiceImpl implements PropertyService {

  private final Logger logger = LoggerFactory.getLogger(Burst.class);
  private static final String LOG_UNDEF_NAME_DEFAULT = "{} undefined. Default: >{}<";
  private final Map<Class<?>, Function<String, ?>> parsers;

  private final Properties properties;

  private final List<String> alreadyLoggedProperties = new ArrayList<>();

  public PropertyServiceImpl(Properties properties) {
    this.properties = properties;
    this.parsers = new HashMap<>();
    parsers.put(String.class, this::getString);
    parsers.put(Integer.class, this::getInt);
    parsers.put(Boolean.class, this::getBoolean);
    parsers.put(List.class, this::getStringList);
  }

  @Override
  public <T> T get(@NotNull Prop<T> prop) {
    return get(prop.name, prop.defaultValue);
  }

  @Override
  public <T> T get(@NotNull String propName, T defaultValue) {
    String value = properties.getProperty(propName);
    if (value == null) return defaultValue;
    try {
      for (Map.Entry<Class<?>, Function<String, ?>> entry : parsers.entrySet()) {
        if (entry.getKey().equals(defaultValue.getClass())) {
          Object parsed = entry.getValue().apply(value);
          if (!defaultValue.getClass().isInstance(parsed)) {
            throw new RuntimeException("Property parser returned type " + parsed.getClass() + ", was looking for type " + defaultValue.getClass());
          }
          logOnce(propName, false, "{}: {}", propName, parsed.toString());
          //noinspection unchecked
          return (T) parsed;
        }
      }
    } catch (Exception e) {
      logger.info("Failed to parse property {}, using default value {}", propName, defaultValue.toString(), e);
    }
    return defaultValue;
  }

  public Boolean getBoolean(String value) {
    if (value.matches("(?i)^1|active|true|yes|on$")) {
      return true;
    }

    if (value.matches("(?i)^0|false|no|off$")) {
      return false;
    }
    throw new IllegalArgumentException();
  }

  public int getInt(String value) {
    int radix = 10;

    if (value != null && value.matches("(?i)^0x.+$")) {
      value = value.replaceFirst("^0x", "");
      radix = 16;
    } else if (value != null && value.matches("(?i)^0b[01]+$")) {
      value = value.replaceFirst("^0b", "");
      radix = 2;
    }

    return Integer.parseInt(value, radix);
  }

  public String getString(String value) {
    if (!value.isEmpty()) {
      return value;
    }

    throw new IllegalArgumentException();
  }

  public List<String> getStringList(String value) {
    if (value.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> result = new ArrayList<>();
    for (String s : value.split(";")) {
      s = s.trim();
      if (!s.isEmpty()) {
        result.add(s);
      }
    }
    return result;
  }

  private void logOnce(String propertyName, boolean debugLevel, String logText, Object... arguments) {
    if (Objects.equals(propertyName, Props.SOLO_MINING_PASSPHRASES.getName())) return;
    if (!this.alreadyLoggedProperties.contains(propertyName)) {
      if (debugLevel) {
        this.logger.debug(logText, arguments);
      } else {
        this.logger.info(logText, arguments);
      }
      this.alreadyLoggedProperties.add(propertyName);
    }
  }
}
