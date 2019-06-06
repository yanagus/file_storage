<#include "security.ftl">

<#macro login path isRegisterForm>
<form action="${path}" method="post">
    <#if message??>
        <div class="form-group row">
            <div class="col-sm-8">
                <div class="alert alert-danger">
                    ${message?ifExists}
                </div>
            </div>
        </div>
    </#if>
    <#if info??>
        <div class="form-group row">
            <div class="col-sm-8">
                <div class="alert alert-info">
                    ${info?ifExists}
                </div>
            </div>
        </div>
    </#if>
    <div class="form-group row">
        <label class="col-sm-2 col-form-label">User Name:</label>
        <div class="col-sm-6">
            <input type="text" name="username" class="form-control ${(usernameError??)?string('is-invalid', '')}"
                                value="<#if user??>${user.username}</#if>" placeholder="User name" />
            <#if usernameError??>
                <div class="invalid-feedback">
                    ${usernameError}
                </div>
            </#if>
        </div>
    </div>

    <div class="form-group row">
        <label class="col-sm-2 col-form-label">Password:</label>
        <div class="col-sm-6">
            <input type="password" name="password" class="form-control ${(passwordError??)?string('is-invalid', '')}"
                                value="<#if user??>${user.password}</#if>" placeholder="Password" />
            <#if passwordError??>
                <div class="invalid-feedback">
                    ${passwordError}
                </div>
            </#if>
        </div>
    </div>

    <#if isRegisterForm>
        <div class="form-group row">
            <label class="col-sm-2 col-form-label">Confirm password:</label>
            <div class="col-sm-6">
                <input type="password" name="password2" class="form-control ${(password2Error??)?string('is-invalid', '')}"
                                    value="<#if user??>${user.password2}</#if>" placeholder="Confirm password" />
                <#if password2Error??>
                    <div class="invalid-feedback">
                        ${password2Error}
                    </div>
                </#if>
            </div>
        </div>
        <div class="form-group row">
            <label class="col-sm-2 col-form-label">E-mail:</label>
            <div class="col-sm-6">
                <input type="email" name="email" class="form-control ${(emailError??)?string('is-invalid', '')}"
                                value="<#if user??>${user.email}</#if>" placeholder="example@example.com" />
                <#if emailError??>
                    <div class="invalid-feedback">
                        ${emailError}
                    </div>
                </#if>
            </div>
        </div>
    </#if>
    <input type="hidden" name="_csrf" value="${_csrf.token}" />
    <#if !isRegisterForm>
        <a href="/registration"><button class="btn btn-primary" type="button">Add new user</button></a>
    </#if>
    <button class="btn btn-primary" type="submit"><#if isRegisterForm>Create<#else>Sign In</#if></button>
</form>
</#macro>

<#macro logout>
    <form action="/logout" method="post">
        <input type="hidden" name="_csrf" value="${_csrf.token}" />
        <button class="btn btn-primary" type="submit" <#if currentUserId == -1>style="display: none;"</#if>>Sign Out</button>
    </form>
</#macro>
