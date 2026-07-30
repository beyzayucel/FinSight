package com.akademi.finsight.news.service;

import com.akademi.finsight.common.constants.SupportedLanguage;

public interface DeepLService {
    String translate(String text, SupportedLanguage language);
}
