<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Отчет по всем преподавателям</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
<#if !embed>
    <link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.css">
    <script src="http://code.jquery.com/jquery-1.12.3.min.js"></script>
    <script src="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.js"></script>
</#if>
    <style>
        th {
            border-bottom: 1px solid #d6d6d6;
        }
        tr:nth-child(even) {
            background: #e9e9e9;
        }

        table, th, td {
            border: 1px solid black;
        }
        td {
            text-align: left;
        }

    </style>
</head>

<body>
<div data-role="page" id="summary">
<#if !embed>
    <div data-role="header">
        <h1>Отчет по всем преподавателям<#if range> за период c ${startDate} по ${endDate}</#if></h1>
    </div>
</#if>
    <div data-role="main" class="ui-content">
        <div data-role="collapsible">
            <h1>Лекции:</h1>
            <table style="width:100%">
                <#--header rows-->
                <tr>
                    <th rowspan="2">Фамилия преподавателя</th>
                    <th colspan="${lectureRatingNum + 1}">За всю историю</th>
                    <#if range>
                        <th colspan="${lectureRatingNum + 1}">За период</th>
                    </#if>
                </tr>
                <tr>
                    <th>Кол-во оценок</th>
                    <#list lectureRatingKeys as ratingKey>
                        <th>${ratingKey}</th>
                    </#list>
                    <#if range>
                        <th>Кол-во оценок</th>
                        <#list lectureRatingKeys as ratingKey>
                            <th>${ratingKey}</th>
                        </#list>
                    </#if>
                </tr>
                <#--data-->
                <#list preps as prep>
                    <#if (prep.getLecturesSummary().entries > 0)>
                        <tr>
                            <td>${prep.name}</td>
                            <td>${prep.getLecturesSummary().entries}</td>
                            <#list prep.getLecturesSummary().getRatingKeys() as key>
                                <td>${prep.getLecturesSummary().getRating(key)}</td>
                            </#list>
                            <#if range>
                                <td>${prep.getLecturesSummary().rangeEntries}</td>
                                <#list prep.getLecturesSummary().getRatingKeys() as key>
                                    <td>${prep.getLecturesSummary().getRangeRating(key)}</td>
                                </#list>
                            </#if>
                        </tr>
                    </#if>
                </#list>

            </table>
        </div>
    </div>

    <div data-role="main" class="ui-content">
        <div data-role="collapsible">
            <h1>Семинары:</h1>
            <table style="width:100%">
            <#--header rows-->
                <tr>
                    <th rowspan="2">Фамилия преподавателя</th>
                    <th colspan="${seminarRatingNum + 1}">За всю историю</th>
                <#if range>
                    <th colspan="${seminarRatingNum + 1}">За период</th>
                </#if>
                </tr>
                <tr>
                    <th>Кол-во оценок</th>
                <#list seminarRatingKeys as ratingKey>
                    <th>${ratingKey}</th>
                </#list>
                <#if range>
                    <th>Кол-во оценок</th>
                    <#list seminarRatingKeys as ratingKey>
                        <th>${ratingKey}</th>
                    </#list>
                </#if>
                </tr>
            <#--data-->
            <#list preps as prep>
                <#if (prep.getSeminarsSummary().entries > 0)>
                    <tr>
                        <td>${prep.name}</td>
                        <td>${prep.getSeminarsSummary().entries}</td>
                        <#list prep.getSeminarsSummary().getRatingKeys() as key>
                            <td>${prep.getSeminarsSummary().getRating(key)}</td>
                        </#list>
                        <#if range>
                            <td>${prep.getSeminarsSummary().rangeEntries}</td>
                            <#list prep.getSeminarsSummary().getRatingKeys() as key>
                                <td>${prep.getSeminarsSummary().getRangeRating(key)}</td>
                            </#list>
                        </#if>
                    </tr>
                </#if>
            </#list>

            </table>
        </div>
    </div>

    <div data-role="main" class="ui-content">
        <div data-role="collapsible">
            <h1>Лабораторные работы:</h1>
            <table style="width:100%"">
            <#--header rows-->
                <tr>
                    <th rowspan="2">Фамилия преподавателя</th>
                    <th colspan="${labRatingNum + 1}">За всю историю</th>
                <#if range>
                    <th colspan="${labRatingNum + 1}">За период</th>
                </#if>
                </tr>
                <tr>
                    <th>Кол-во оценок</th>
                <#list labRatingKeys as ratingKey>
                    <th>${ratingKey}</th>
                </#list>
                <#if range>
                    <th>Кол-во оценок</th>
                    <#list labRatingKeys as ratingKey>
                        <th>${ratingKey}</th>
                    </#list>
                </#if>
                </tr>
            <#--data-->
            <#list preps as prep>
                <#if (prep.getLabSummary().entries > 0)>
                    <tr>
                        <td>${prep.name}</td>
                        <td>${prep.getLabSummary().entries}</td>
                        <#list prep.getLabSummary().getRatingKeys() as key>
                            <td>${prep.getLabSummary().getRating(key)}</td>
                        </#list>
                        <#if range>
                            <td>${prep.getLabSummary().rangeEntries}</td>
                            <#list prep.getLabSummary().getRatingKeys() as key>
                                <td>${prep.getLabSummary().getRangeRating(key)}</td>
                            </#list>
                        </#if>
                    </tr>
                </#if>
            </#list>

        </div>
    </div>

<#if !embed>
    <div data-role="footer">
        <h1>Создано при помощи <a href="https://bitbucket.org/mipt-npm/mipt-physics-survey">генератора отчетов</a>. &copy; <a href="mailto:altavir@gmail.com">Александр Нозик</a>, 2016</h1>
    </div>
</#if>
</div>
</body>
</html>