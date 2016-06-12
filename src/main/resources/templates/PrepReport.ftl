<#assign lectures=prep.getLecturesSummary()>
<#assign seminars=prep.getSeminarsSummary()>
<#assign labs=prep.getLabSummary()>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Отчет по преподавателю $[prep.getName()}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <#if !embed>
        <link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.css">
        <script src="http://code.jquery.com/jquery-1.12.3.min.js"></script>
        <script src="http://code.jquery.com/mobile/1.4.5/jquery.mobile-1.4.5.min.js"></script>
    </#if>
</head>

<body>
    <div data-role="page" id="report">
        <#if !embed>
            <div data-role="header">
                <h1>${prep.getName()}</h1>
            </div>
        </#if>
        <#if (lectures.entries > 0)>
            <div data-role="main" class="ui-content">
                <div data-role="collapsible">
                    <h1>Лекции:</h1>
                    <h2>Оценки:</h2>
                    <p><strong>Количество оценок:</strong> ${lectures.entries}</p>
                    <#list lectures.ratings?keys as key>
                        <p><strong>${key}:</strong> ${lectures.getRating(key)}</p>
                    </#list>
                    <div data-role="collapsible">
                        <h2>Отзывы:</h2>
                        <#list lectures.getComments() as comment>
                            <p><strong>${comment.first}</strong></p>
                            <p>${comment.second}</p>
                        </#list>
                    </div>
                </div>
            </div>
        </#if>
        <#if (seminars.entries > 0)>
            <div data-role="main" class="ui-content">
                <div data-role="collapsible">
                    <h1>Семинары:</h1>
                    <h2>Оценки:</h2>
                    <p><strong>Количество оценок:</strong> ${seminars.entries}</p>
                    <#list seminars.ratings?keys as key>
                        <p><strong>${key}:</strong> ${seminars.getRating(key)}</p>
                    </#list>
                    <div data-role="collapsible">
                        <h2>Отзывы:</h2>
                        <#list seminars.getComments() as comment>
                            <p><strong>${comment.first}</strong></p>
                            <p>${comment.second}</p>
                        </#list>
                    </div>
                </div>
            </div>
        </#if>
        <#if (labs.entries > 0)>
            <div data-role="main" class="ui-content">
                <div data-role="collapsible">
                    <h1>Лабораторные работы:</h1>
                    <h2>Оценки:</h2>
                    <p><strong>Количество оценок:</strong> ${labs.entries}</p>
                    <#list labs.ratings?keys as key>
                        <p><strong>${key}:</strong> ${labs.getRating(key)}</p>
                    </#list>
                    <div data-role="collapsible">
                        <h2>Отзывы:</h2>
                        <#list labs.getComments() as comment>
                            <p><strong>${comment.first}</strong></p>
                            <p>${comment.second}</p>
                        </#list>
                    </div>
                </div>
            </div>
        </#if>
        <#if !embed>
            <div data-role="footer">
                <h1>Создано при помощи <a href="https://bitbucket.org/mipt-npm/mipt-physics-survey">генератора отчетов</a>. &copy; <a href="mailto:altavir@gmail.com">Александр Нозик</a>, 2016</h1>
            </div>
        </#if>
    </div>
</body>
</html>