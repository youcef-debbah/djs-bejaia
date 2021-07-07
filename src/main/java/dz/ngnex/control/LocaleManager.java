/*
 * Handcrafted with love by Youcef DEBBAH
 * Copyright 2019 youcef-debbah@hotmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dz.ngnex.control;

import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.Messages;
import dz.ngnex.util.TemplatedContent;
import dz.ngnex.util.WebKit;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author youcef debbah
 */
@Named
@RequestScoped
@InjectableByTests
public class LocaleManager implements Serializable {

    private static final long serialVersionUID = -7848606304114310427L;
    public static final String NO_DATE_KEY = "nothing";

    private static Logger log = LogManager.getLogger(LocaleManager.class);

    public static final ZoneId ADMIN_ZONE = ZoneId.of("GMT+1");
    public static final TimeZone ADMIN_TIME_ZONE = TimeZone.getTimeZone(ADMIN_ZONE);
    private static final LocalDate FIRST_DAY = LocalDate.of(2019, 7, 15);

    private static final String LOCAL_DATE_TIME_FORMAT = "HH:mm:ss dd/MM/yyyy";
    private static final String LOCAL_DATE_FORMAT = "dd/MM/yyyy";
    private static final String DETAILED_LOCAL_DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCAL_TIME_FORMAT = "HH:mm:ss";
    private static final String FULL_DATE_TIME_FORMAT = "HH:mm:ss.SSS yyyy-MM-dd";

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT);
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT);
    private static final DateTimeFormatter DETAILED_LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern(DETAILED_LOCAL_DATE_FORMAT);
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_TIME_FORMAT);

    private static final ThreadLocal<DecimalFormat> DA_FORMATTERS = ThreadLocal.withInitial(LocaleManager::newDAFormatter);
    private final static String ZERO_DA = newDAFormatter().format(BigDecimal.ZERO);

    @Inject
    private PrincipalPreferences preferences;

    @Inject
    private Messages messages;

    @PostConstruct
    private void init() {
        HttpServletRequest request = WebKit.findFacesRequest();
        if (request != null)
            preferences.init(request);
    }

    private static DecimalFormat newDAFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRENCH);
        symbols.setGroupingSeparator(' ');
        return new DecimalFormat("#,##0.00 DA", symbols);
    }

    public Locale getLocale() {
        return preferences.getLocale();
    }

    public String getLanguage() {
        return preferences.getLocale().getLanguage();
    }

    public boolean isLanguageEquals(String lang) {
        return preferences.getLocale().getLanguage().equalsIgnoreCase(new Locale(lang).getLanguage());
    }

    public void changeLang(String language) {
        preferences.setLocale(new Locale(language));
        PrimeFaces.current().executeScript("location.reload(true);");
    }

    public String formatAsDA(BigDecimal decimal) {
        if (decimal != null)
            return DA_FORMATTERS.get().format(decimal);
        else
            return ZERO_DA;
    }

    public String formatAsDA_HTML(BigDecimal decimal) {
        return "<span style='display: inline-block'>" + formatAsDA(decimal) + "</span>";
    }

    public LocalDate getFirstDay() {
        return FIRST_DAY;
    }

    public ZoneId getAdminZone() {
        return ADMIN_ZONE;
    }

    public TimeZone getAdminTimeZone() {
        return ADMIN_TIME_ZONE;
    }

    public String getZeroDinar() {
        return ZERO_DA;
    }

    @NotNull
    public String formatAsLocalDateTime(Long epoch) {
        if (epoch == null)
            return messages.get(NO_DATE_KEY);
        else
            return Instant.ofEpochMilli(epoch).atZone(ADMIN_ZONE).format(LOCAL_DATE_TIME_FORMATTER);
    }

    @NotNull
    public String formatAsLocalDate(Long epoch) {
        if (epoch == null)
            return messages.get(NO_DATE_KEY);
        else
            return Instant.ofEpochMilli(epoch).atZone(ADMIN_ZONE).format(LOCAL_DATE_FORMATTER);
    }

    @NotNull
    public String formatAsDetailedLocalDate(Long epoch) {
        if (epoch == null)
            return messages.get(NO_DATE_KEY);
        else
            return Instant.ofEpochMilli(epoch).atZone(ADMIN_ZONE).format(DETAILED_LOCAL_DATE_FORMATTER);
    }

    @NotNull
    public String formatAsLocalTime(Long epoch) {
        if (epoch == null)
            return messages.get(NO_DATE_KEY);
        else
            return Instant.ofEpochMilli(epoch).atZone(ADMIN_ZONE).format(LOCAL_TIME_FORMATTER);
    }

    public Date getBeginningOfTime() {
        return Date.from(FIRST_DAY.atStartOfDay(ADMIN_ZONE).toInstant());
    }

    public Date getCurrentTime() {
        return new Date(getCurrentEpoch());
    }

    public long getCurrentEpoch() {
        return Instant.now(Clock.system(ADMIN_ZONE)).toEpochMilli();
    }

    public Date toDate(Long epoch) {
        if (epoch == null)
            return null;
        else
            return new Date(epoch);
    }

    public LocalDate getCurrentDate() {
        return LocalDate.now(ADMIN_ZONE);
    }

    public LocalDate toLocalDate(Date date) {
        Objects.requireNonNull(date);
        return date.toInstant().atZone(ADMIN_ZONE).toLocalDate();
    }

    public String getCurrentLocalDateTime() {
        return LOCAL_DATE_TIME_FORMATTER.format(LocalDateTime.now(getAdminZone()));
    }

    public String formatTextAsHtml(String text) {
        if (text == null || text.isEmpty())
            return text;
        else
            return TemplatedContent.newLinesToBrElements(StringEscapeUtils.escapeHtml4(TemplatedContent.brElementsToNewLines(text)));
    }

    public Long asLong(Object object) {
        try {
            if (object instanceof Number)
                return ((Number) object).longValue();
            else if (object instanceof String)
                return Long.valueOf((String) object);
            else
                return null;
        } catch (RuntimeException e) {
            log.warn("could not parse object as long: " + object);
            return null;
        }
    }
}
