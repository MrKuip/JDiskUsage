package org.kku.jdiskusage.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log
{
  static public final MyLogger log = createLogger("log", "log", 100000, 10, Level.FINE);

  private Log()
  {
  }

  static private MyLogger createLogger(String name, String fileName, long fileSize, int count, Level level)
  {
    Logger logger;
    Handler handler;

    logger = Logger.getLogger(name);
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    handler = new MyConsoleHandler();
    handler.setFormatter(getFormatter());
    logger.addHandler(handler);
    handler = createFileHandler(fileName, fileSize, count, true);
    handler.setFormatter(getFormatter());
    logger.addHandler(handler);

    return new MyLogger(logger);
  }

  static private Handler createFileHandler(String fileName, long limit, int count, boolean append)
  {
    // HACK: The FileHandler doesn't create parent directories of the pattern.
    //       It will throw a NoSuchFileException with a message that is the name of the path.
    //       Then create the directories and try again.
    for (int i = 0; i < 2; i++)
    {
      try
      {
        return new FileHandler("%t/jdiskusage/" + fileName + "%g.log", limit, count, append);
      }
      catch (NoSuchFileException e)
      {
        if (i == 0)
        {
          Path path;

          path = Paths.get(e.getMessage());
          if (path != null)
          {
            try
            {
              Files.createDirectories(path.getParent());
            }
            catch (IOException e1)
            {
              e1.printStackTrace();
            }
          }
        }
        else
        {
          e.printStackTrace();
        }
      }
      catch (SecurityException | IOException e)
      {
        e.printStackTrace();
      }
    }

    return null;
  }

  static private Formatter getFormatter()
  {
    return new Formatter()
    {
      @Override
      public String format(LogRecord logRecord)
      {
        String msg;

        msg = String.format("%1$tF %1$tT %2$s%n", Date.from(logRecord.getInstant()), logRecord.getMessage());
        if (logRecord.getThrown() != null)
        {
          try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw))
          {
            logRecord.getThrown().printStackTrace(pw);
            msg += sw.toString();
          }
          catch (IOException e)
          {
            // This will never happen
            e.printStackTrace();
          }
        }

        return msg;
      }
    };
  }

  static public class MyLogger
  {
    private final Logger mi_logger;

    private MyLogger(Logger logger)
    {
      mi_logger = logger;
    }

    public void setLevel(Level level)
    {
      mi_logger.setLevel(level);
    }

    public void info(String msg)
    {
      mi_logger.log(Level.INFO, msg);
    }

    public void info(String format, Object... args)
    {
      log(Level.INFO, null, format, args);
    }

    public void debug(String msg)
    {
      mi_logger.log(Level.FINE, msg);
    }

    public void debug(String format, Object... args)
    {
      log(Level.FINE, null, format, args);
    }

    public void error(Throwable throwable, String format, Object... args)
    {
      log(Level.SEVERE, throwable, String.format(format, args));
    }

    private void log(Level level, Throwable throwable, String format, Object... args)
    {
      if (mi_logger.isLoggable(level))
      {
        mi_logger.log(Level.SEVERE, String.format(format, args), throwable);
      }
    }
  }

  static public class MyConsoleHandler
    extends ConsoleHandler
  {
    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW")
    public MyConsoleHandler()
    {
      setOutputStream(System.out);
    }
  }
}
