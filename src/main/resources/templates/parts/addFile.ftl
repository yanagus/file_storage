<h5>Add file</h5>
<div>
    <form method="post" enctype="multipart/form-data">
        <input type="file" name="file">
        <input type="hidden" name="id" value="<#if file??>${file.id}</#if>" />
        <button class="btn btn-primary" type="submit">Add</button>
    </form>
</div>