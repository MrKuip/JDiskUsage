package org.kku.main;

public class Test
{
  private void compile()
  {
    String code;

    code = """
        package org.kku.jdiskusage.ui;

        import org.kku.jdiskusage.util.FileTree.FileNodeIF;
        import org.kku.jdiskusage.util.FileTree.FilterIF;

          public class MyFilter
              implements FilterIF
          {
            @Override
            public boolean accept(FileNodeIF path)
            {
              return path.getNumberOfLinks() < 1;
            }
          }""";

    try
    {
      // Class aClass =
      // CompilerUtils.CACHED_COMPILER.loadFromJava("org.kku.jdiskusage.ui.MyFilter",
      // code);
    }
    catch (Exception ex)
    {
    }
  }

  public static void main(String[] args)
  {
    new Test().compile();
  }
}
