<#import "parts/common.ftl" as c>

<@c.page>
<div class="col-sm-8">
    <div class="alert alert-danger">
        ${error?ifExists}
    </div>
</div>
</@c.page>