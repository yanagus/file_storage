<#include "parts/security.ftl">
<#import "parts/common.ftl" as c>

<@c.page>
<h5>List of users</h5>

<table class="table table-striped">
    <thead class="thead-light">
        <tr>
            <th>Name</th>
        </tr>
    </thead>
    <tbody>
    <#list users as user>
        <tr>
            <td><a href="/${user.id}/files">${user.username}</a></td>
        </tr>
    </#list>
    </tbody>
</table>
</@c.page>
