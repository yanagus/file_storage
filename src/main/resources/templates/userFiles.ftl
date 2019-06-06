<#include "parts/security.ftl">
<#import "parts/common.ftl" as c>

<@c.page>
<#if isFileOwner>
    <#include "parts/addFile.ftl" />
</#if>

<h3>File list of ${fileOwner.username}</h3>

<#if info??>
    <div class="alert alert-info">
        ${info?ifExists}
    </div>
</#if>

<#if !isFileOwner>
    <form action="/askread/${fileOwner.id}" method="post" class="form-check form-check-inline">
        <input type="hidden" name="_csrf" value="${_csrf.token}" />
        <button class="btn btn-info mb-2" type="submit">Ask access to read</button>
    </form>
    <form action="/askdownload/${fileOwner.id}" method="post" class="form-check form-check-inline">
        <input type="hidden" name="_csrf" value="${_csrf.token}" />
        <button class="btn btn-info mb-2" type="submit">Ask access to download</button>
    </form>
</#if>

<#include "parts/fileList.ftl" />
</@c.page>