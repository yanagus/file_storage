<h5>Add file</h5>

<div class="form-group mt-3">
    <form method="post" enctype="multipart/form-data">
        <input type="file" name="file">
        <input type="hidden" name="_csrf" value="${_csrf.token}" />
        <button class="btn btn-primary" type="submit">Add file</button>
    </form>
</div>

