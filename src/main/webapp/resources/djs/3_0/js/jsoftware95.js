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

var jsoftware95 = jsoftware95 || {};
var messages = messages || {};

jsoftware95.altImage = function (imageElement, altImage) {
    imageElement.onerror = "";
    if (imageElement.src !== altImage)
        imageElement.src = altImage;
    return true;
};

jsoftware95.scrollToBottom = function (id) {
    let element = document.getElementById(id);
    element.scrollTop = element.scrollHeight;
};

jsoftware95.selectTabWithMessages = function (widgets) {
    function containsErrors(element) {
        return element.getElementsByClassName("ui-state-error").length > 0
            || element.getElementsByClassName("ui-messages-error").length > 0
            || element.getElementsByClassName("ui-message-error").length > 0;
    }

    widgets.forEach(widget => {
        let tabView = PF(widget);
        let tabRoot = document.getElementById(tabView.id);
        let tabs = tabRoot.getElementsByClassName("ui-tabs-panel");

        if (containsErrors(tabs[tabView.getActiveIndex()]))
            jsoftware95.invalidInput();
        else
            for (let i = 0; i < tabView.getLength(); i++)
                if (containsErrors(tabs[i])) {
                    tabView.select(i);
                    jsoftware95.invalidInput();
                    break;
                }
    });


};

jsoftware95.deleteSelectedRow = function (outputID) {
    const sheet = PF('main_sheet_widget');
    const selection = document.getElementById(sheet.id + '_selection').value;
    if (selection.length > 0) {
        const selectedCells = JSON.parse(selection);
        const startRow = selectedCells[0];
        const endRow = selectedCells[2];

        const rowIndexes = $(sheet.tableDiv).find(".row_index").map(function (index, cell) {
            if (index >= startRow && index <= endRow)
                return cell.textContent;
            else
                return null;
        }).toArray();

        document.getElementById(outputID).value = Object.values(rowIndexes);
    }
};

jsoftware95.filterTableRowsBy = function (input, table) {
    const filter = input.toUpperCase();
    const tr = table.find("tr");
    for (let i = 0; i < tr.length; i++) {
        if (tr[i].textContent.toUpperCase().indexOf(filter) > -1) {
            tr[i].style.display = "";
        } else {
            tr[i].style.display = "none";
        }
    }

    PF('sheet_scroll_widget').redraw();
};

jsoftware95.info = function (summary, detail) {
    PF('global_growl_widget').renderMessage({
        "summary": summary,
        "detail": detail,
        "severity": "info"
    })
};

jsoftware95.warn = function (summary, detail) {
    PF('global_growl_widget').renderMessage({
        "summary": summary,
        "detail": detail,
        "severity": "warn"
    })
};

jsoftware95.error = function (summary, detail) {
    PF('global_growl_widget').renderMessage({
        "summary": summary,
        "detail": detail,
        "severity": "error"
    })
};

jsoftware95.success = function (summary) {
    jsoftware95.info(summary, messages.successfully);
};

jsoftware95.failed = function (summary) {
    jsoftware95.error(summary, messages.failed);
};

jsoftware95.updateFailed = function () {
    jsoftware95.error(messages.updateFailed, messages.canNotConnect);
};

jsoftware95.invalidInput = function () {
    jsoftware95.error(messages.invalidInput, messages.recheckSubmittedValues);
};

jsoftware95.findParentFadeOut_removeSlowly = function (el, sel) {
    jsoftware95.fadeOut_removeSlowly(jsoftware95.findParent(el, sel));
};

jsoftware95.findParent = function (el, sel) {
    while ((el = el.parentElement) && !((el.matches || el.matchesSelector).call(el, sel))) ;
    return el;
};

jsoftware95.fadeOut_removeSlowly = function (target) {
    target.classList.add('animated');
    target.classList.add('fadeOutRightBig');
    setTimeout(jsoftware95.removeSlowly, 200, target);
};

jsoftware95.removeSlowly = function (target) {
    const element = $(target);
    element.hide('slow', function () {
        element.remove();
    });
};

jsoftware95.init = function () {

    PrimeFaces.locales['fr'] = {
        closeText: 'Fermer',
        prevText: 'Précédent',
        nextText: 'Suivant',
        monthNames: ['Janvier', 'Février', 'Mars', 'Avril', 'Mai',
            'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre',
            'Novembre', 'Décembre'],
        monthNamesShort: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun',
            'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'],
        dayNames: ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi',
            'Vendredi', 'Samedi'],
        dayNamesShort: ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'],
        dayNamesMin: ['D', 'L', 'M', 'M', 'J', 'V', 'S'],
        weekHeader: 'Semaine',
        firstDay: 1,
        isRTL: false,
        showMonthAfterYear: false,
        yearSuffix: '',
        timeOnlyTitle: 'Choisir l\'heure',
        timeText: 'Heure',
        hourText: 'Heures',
        minuteText: 'Minutes',
        secondText: 'Secondes',
        currentText: 'Maintenant',
        ampm: false,
        month: 'Mois',
        week: 'Semaine',
        day: 'Jour',
        allDayText: 'Toute la journée'
    };

    PrimeFaces.locales['ar'] = {
        closeText: 'إغلق',
        prevText: 'إلى الخلف',
        nextText: 'إلى الأمام',
        currentText: 'بداية',
        monthNames: ['ديسمبر', 'نوفمبر', 'أكتوبر', 'سبتمبر', 'أغسطس',
            'يوليو', 'يونيو', 'مايو', 'ابريل', 'مارس', 'فبراير',
            'يناير'],
        monthNamesShort: ['ديسمبر', 'نوفمبر', 'أكتوبر', 'سبتمبر',
            'أغسطس', 'يوليو', 'يونيو', 'مايو', 'ابريل', 'مارس',
            'فبراير', 'يناير'],
        dayNames: ['يوم الأحد‎', 'يوم الإثنين‎', 'يوم الثلاثاء‎',
            '‏يوم الأَرْبعاء‎', '‏يوم الخَمِيس‎', 'يوم الجُمْعَة‎‎',
            'يوم السَّبْت'],
        dayNamesShort: ['الأحد‎', 'الإثنين‎', 'الثلاثاء‎', 'الأَرْبعاء‎',
            'الخَمِيس‎', 'الجُمْعَة‎‎', 'السَّبْت'],
        dayNamesMin: ['الأحد‎', 'الإثنين‎', 'الثلاثاء‎', 'الأَرْبعاء‎',
            'الخَمِيس‎', 'الجُمْعَة‎‎', 'السَّبْت'],
        weekHeader: 'الأسبوع',
        firstDay: 1,
        isRTL: false,
        showMonthAfterYear: false,
        yearSuffix: '',
        timeOnlyTitle: 'الوقت فقط',
        timeText: 'الوقت',
        hourText: 'ساعة',
        minuteText: 'دقيقة',
        secondText: 'ثانية',
        ampm: false,
        month: 'الشهر',
        week: 'الأسبوع',
        day: 'اليوم',
        allDayText: 'سا ئراليوم'
    };

};

jsoftware95.init();