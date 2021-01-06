<!DOCTYPE html>
<html>
<body>
<style>
    .main{
        position:fixed;
        margin-top: 100px;
        left: 50%;
        transform: translate(-50%, -50%);

    }
    .wrap {
        float: left; /* 自适应内容宽度 */
        position: relative;
        left: 50%;
    }
    .inner {
        position: relative;
        left: -50%;
    }

    table {
        width: 90%;
        background: #ccc;
        margin: 10px auto;
        border-collapse: collapse;
        /*border-collapse:collapse合并内外边距
        (去除表格单元格默认的2个像素内外边距*/
    }
    th,td {
        height: 25px;
        line-height: 25px;
        text-align: center;
        border: 1px solid #ccc;
    }
    th {
        background: #eee;
        font-weight: normal;
    }
    tr {
        background: #fff;
    }
    tr:hover {
        background: #cc0;
    }
    td a {
        color: #06f;
        text-decoration: none;
    }
    td a:hover {
        color: #06f;
        text-decoration: underline;
    }
</style>
<script type="text/javascript">

    function search(){
        window.location.href = "http://localhost:8080/?key="+document.getElementById('key').value;
    }

</script>
<div  class="wrap">
<input type="text" id="key" value="${key}"/>
<button id="btn" onclick="search()">搜索</button>
    <div class="inner">
</br>
    <table border="1">
        <tr>
            <td>title</td>
            <td>overview</td>
        </tr>
<#list movies as movie>
    <tr>

    <td><@movie.title?interpret /></td>
        <td><@movie.overview?interpret /></td>
    </tr>
</#list>
    </table>
    </div>
</div>
</body>
</html>