package org.kku.jdiskusage.conf;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LanguageConfigurationTest
{
  @Test
  void testDefault()
  {
    List<Language> list;

    list = LanguageConfiguration.getInstance().getList().stream().filter(Language::isDefault).toList();
    assertTrue(list.size() != 0, "There must be 1 default. Check LanguageConfiguration.json");
    assertTrue(list.size() == 1, "There can only be 1 default. Check LanguageConfiguration.json");
    assertTrue(LanguageConfiguration.getInstance().getDefault().isDefault(),
        "The default language method isDefault() doesnt not return true");
    assertNotNull(LanguageConfiguration.getInstance().getDefault() != null);
  }

  static Stream<Language> provideLanguageStream()
  {
    return LanguageConfiguration.getInstance().getList().stream();
  }

  @ParameterizedTest
  @MethodSource("provideLanguageStream")
  void testLanguage(Language language)
  {
    assertNotNull(language.getFlag(),
        "The flag must not be null. Execute 'jar -tvf lib/flagpack-flags.jar' to search for a name");
    assertNotNull(language.getFlagImage(), "The flag " + language.getFlag()
        + " is not valid! Execute 'jar -tvf lib/flagpack-flags.jar' to search for a valid name");
    assertNotNull(language.getName(), "The 'name' of a language must not be null.");
    assertNotNull(language.getLanguage(), "The 'language' of a language must not be null.");
    assertNotNull(language.getLocale(), "The 'locale' of a language must not be null.");
    assertTrue(
        Stream.of(Locale.getAvailableLocales()).filter(locale -> locale.equals(language.getLocale())).findFirst()
            .isPresent(),
        "The 'locale' " + language.getLocale() + " is not valid! It cannot be found in Locale.getAvailableLocales()");
  }
}
