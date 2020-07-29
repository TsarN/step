package com.google.sps;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.util.List;

public class Translator {
  private Translate translateService = TranslateOptions.newBuilder()
      .setProjectId("tsarn-step-2020")
      .setQuotaProjectId("tsarn-step-2020")
      .build().getService();

  public String translate(String text, String languageCode) {
    if (languageCode == null) {
      return text;
    }
    Translation translation =
        translateService.translate(text, Translate.TranslateOption.targetLanguage(languageCode));

    return translation.getTranslatedText();
  }

  public List<Language> getLanguages() {
    return translateService.listSupportedLanguages();
  }
}
