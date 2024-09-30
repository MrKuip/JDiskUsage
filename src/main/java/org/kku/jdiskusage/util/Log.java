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
  static public final MyLogger log = createLogger("log", "log", 100000, 10, Level.INFO);
  static public final MyLogger treemap = createLogger("treemap", "treemap", 100000, 10, Level.INFO);
  static public final MyLogger javafx = createLogger("javafx", Level.INFO);

  private Log()
  {
  }

  static private MyLogger createLogger(String name, Level level)
  {
    Logger logger;

    logger = Logger.getLogger(name);
    logger.setLevel(level);
    logger.setUseParentHandlers(false);
    logger.addHandler(new MyConsoleHandler());

    return new MyLogger(logger);
  }

  static private MyLogger createLogger(String name, String fileName, long fileSize, int count, Level level)
  {
    MyLogger logger;

    logger = createLogger(name, level);
    logger.addHandler(createFileHandler(fileName, fileSize, count, true));

    return logger;
  }

  /**
   * Create a file handler that will roll over when it's limit is reached.<br>
   * 
   * The log files are placed in the directory 'temp'/jdiskusage.<br>
   * The intermediate directories are created if they do not exist.<br>
   * 
   * @param limit  the maximum number of bytes to write to any one file
   * @param count  the number of files to use
   * @param append  specifies append mode
   * @throws  IOException if there are IO problems opening the files.
   * @throws  SecurityException  if a security manager exists and if
   *             the caller does not have {@code LoggingPermission("control")}.
   * @throws  IllegalArgumentException if {@code limit < 0}, or {@code count < 1}.
   * @throws  IllegalArgumentException if pattern is an empty string
   * 
   * @see FileHandler#FileHandler(String, long, int, boolean)
   */

  static private Handler createFileHandler(String fileName, long limit, int count, boolean append)
  {
    // HACK: The FileHandler doesn't create parent directories of the pattern.
    //       It will throw a NoSuchFileException with a message that is the name of the path.
    //       Then create the directories and try again.
    for (int i = 0; i < 2; i++)
    {
      try
      {
        FileHandler handler;

        handler = new FileHandler("%t/jdiskusage/" + fileName + "%g.log", limit, count, append);
        handler.setFormatter(createDefaultFormatter());

        return handler;
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

  /**
   * Create a formatter that outputs 1 line.
   */
  static private Formatter createDefaultFormatter()
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

  /**
   * Wrapper around Logger that adds varargs
   */
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

    public void addHandler(Handler handler)
    {
      mi_logger.addHandler(handler);
    }

    public void info(String msg)
    {
      log(Level.INFO, null, msg);
    }

    public void info(String format, Object... args)
    {
      log(Level.INFO, null, format, args);
    }

    public void fine(String msg)
    {
      log(Level.FINE, null, msg);
    }

    public void fine(String format, Object... args)
    {
      log(Level.FINE, null, format, args);
    }

    public void finer(String msg)
    {
      log(Level.FINER, null, msg);
    }

    public void finer(String format, Object... args)
    {
      log(Level.FINER, null, format, args);
    }

    public void finest(String msg)
    {
      log(Level.FINEST, null, msg);
    }

    public void finest(String format, Object... args)
    {
      log(Level.FINEST, null, format, args);
    }

    public void error(String format, Object... args)
    {
      error(null, format, args);
    }

    public void error(Throwable throwable, String format, Object... args)
    {
      log(Level.SEVERE, throwable, format, args);
    }

    private void log(Level level, Throwable throwable, String text)
    {
      if (mi_logger.isLoggable(level))
      {
        mi_logger.log(level, text, throwable);
      }
    }

    private void log(Level level, Throwable throwable, String format, Object... args)
    {
      if (mi_logger.isLoggable(level))
      {
        mi_logger.log(level, String.format(format, args), throwable);
      }
    }
  }

  /**
   *  Handler that prints all log records to standard out with our default formatter.
   */
  static public class MyConsoleHandler
    extends ConsoleHandler
  {
    @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW")
    public MyConsoleHandler()
    {
      setOutputStream(System.out);
      setLevel(Level.ALL);
      setFormatter(createDefaultFormatter());
    }
  }
}
