package org.kku.jdiskusage.util;

import java.io.IOException;
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
  static public MyLogger performance = createLogger("performance", "performance", 100000, 10);
  static public MyLogger log = createLogger("log", "log", 100000, 10);

  {
    performance.setLevel(Level.INFO);
    log.setLevel(Level.INFO);
  }

  private Log()
  {
  }

  static private MyLogger createLogger(String name, String fileName, long fileSize, int count)
  {
    Logger logger;
    Handler handler;

    logger = Logger.getLogger(name);
    logger.setUseParentHandlers(false);
    handler = new MyConsoleHandler();
    handler.setFormatter(getFormatter());
    logger.addHandler(handler);

    // HACK: The FileHandler doesn't create parent directories of the pattern.
    //       It will throw a NoSuchFileException with a message that is the name of the path.
    //       
    for (int i = 0; i < 2; i++)
    {
      try
      {
        handler = new FileHandler("%t/jdiskusage/" + fileName + "%g.log", fileSize, count, true);
        handler.setFormatter(getFormatter());
        logger.addHandler(handler);
        break;
      }
      catch (NoSuchFileException e)
      {
        if (i == 0)
        {
          Path path = Paths.get(e.getMessage());
          try
          {
            Files.createDirectories(path.getParent());
          }
          catch (IOException e1)
          {
            e1.printStackTrace();
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

    return new MyLogger(logger);
  }

  static private Formatter getFormatter()
  {
    return new Formatter()
    {
      @Override
      public String format(LogRecord record)
      {
        return String.format("%1$tF %1$tT %2$s%n", Date.from(record.getInstant()), record.getMessage());
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

    private void setLevel(Level level)
    {
      mi_logger.setLevel(level);
    }

    public void info(String msg)
    {
      mi_logger.info(msg);
    }

    public void info(String format, Object... args)
    {
      mi_logger.info(String.format(format, args));
    }

    public void debug(String msg)
    {
      mi_logger.fine(msg);
    }

    public void debug(String format, Object... args)
    {
      mi_logger.fine(String.format(format, args));
    }
  }

  static public class MyConsoleHandler
    extends ConsoleHandler
  {
    public MyConsoleHandler()
    {
      setOutputStream(System.out);
    }
  }
}
