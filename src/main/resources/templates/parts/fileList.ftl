<table class="table table-striped">
    <thead class="thead-light">
        <tr>
            <th>File Name</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <#list files as file>
            <tr>
                <td><a href="/files/${file.id}"><b>${file.originalName}</b></a></td>
                <td><button class="btn btn-primary" onclick="deleteData(${file.id})">Delete</button></td>
            </tr>
        </#list>
    </tbody>
</table>

<script type="text/javascript">
    function deleteData(id) {
        fetch('/files/' + id, {
            method: 'DELETE'
            }).then(function(response) {
                return response.text();
            }).then(function(html) {
                document.documentElement.innerHTML = html;
            });
    }
</script>