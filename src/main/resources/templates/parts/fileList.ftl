<#include "security.ftl">

    <table class="table table-striped">
        <thead class="thead-light">
            <tr>
                <th>File Name</th>
                <th>User Name</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <#list files as file>
                <tr>
                    <#if readAccess>
                        <td><b>${file.originalName}</b></td>
                    <#else>
                        <td><a href="/files/${file.id}"><b>${file.originalName}</b></a></td>
                    </#if>

                    <td><a href="/${file.user.id}/files">${file.user.username}</a></td>

                    <#if file.user.id == currentUserId>
                            <td><button class="btn btn-primary" onclick="deleteData(${currentUserId}, ${file.id})">Delete</button></td>
                            <input type="hidden" name="_csrf" value="${_csrf.token}"/>
                    <#else>
                        <td> </td>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>

<script type="text/javascript">
    function deleteData(userId, id) {
        var token = $("input[name='_csrf']").val();
        fetch('/' + userId + '/files/' + id, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': token
            },
            credentials: 'include'
            }).then(function(response) {
                return response.text();
            }).then(function(html) {
                document.documentElement.innerHTML = html;
            });
    }
</script>