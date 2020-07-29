package com.google.sps;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class Translator {
  public static String translate(String text, String languageCode) {
    if (languageCode == null) {
      return text;
    }

    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation =
        translate.translate(text, Translate.TranslateOption.targetLanguage(languageCode));

    return translation.getTranslatedText();
  }
}
