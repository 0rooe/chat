document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('registerForm');
    const usernameInput = document.getElementById('username');
    const nicknameInput = document.getElementById('nickname');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthBar = document.getElementById('strengthBar');
    const submitBtn = form.querySelector('.submit-btn');

    // 输入验证
    usernameInput.addEventListener('input', validateUsername);
    nicknameInput.addEventListener('input', validateNickname);
    passwordInput.addEventListener('input', validatePassword);
    confirmPasswordInput.addEventListener('input', validateConfirmPassword);

    // 表单提交
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (validateAllFields()) {
            submitForm();
        }
    });

    // 用户名验证
    function validateUsername() {
        const username = usernameInput.value.trim();
        const isValid = /^[a-zA-Z0-9_]{3,50}$/.test(username);
        
        clearFieldError(usernameInput);
        
        if (username === '') {
            setFieldState(usernameInput, 'normal');
        } else if (isValid) {
            setFieldState(usernameInput, 'success');
        } else {
            setFieldState(usernameInput, 'error');
            showFieldError(usernameInput, '用户名格式不正确，请使用3-50个字母、数字或下划线');
        }
        
        return isValid || username === '';
    }

    // 昵称验证
    function validateNickname() {
        const nickname = nicknameInput.value.trim();
        const isValid = nickname.length > 0 && nickname.length <= 20;
        
        clearFieldError(nicknameInput);
        
        if (nickname === '') {
            setFieldState(nicknameInput, 'normal');
        } else if (isValid) {
            setFieldState(nicknameInput, 'success');
        } else {
            setFieldState(nicknameInput, 'error');
            showFieldError(nicknameInput, '昵称不能为空且不能超过20个字符');
        }
        
        return isValid || nickname === '';
    }

    // 密码验证
    function validatePassword() {
        const password = passwordInput.value;
        const strength = calculatePasswordStrength(password);
        
        updatePasswordStrength(strength);
        clearFieldError(passwordInput);
        
        if (password === '') {
            setFieldState(passwordInput, 'normal');
            return true;
        } else if (password.length >= 6) {
            setFieldState(passwordInput, 'success');
            return true;
        } else {
            setFieldState(passwordInput, 'error');
            showFieldError(passwordInput, '密码至少需要6个字符');
            return false;
        }
    }

    // 确认密码验证
    function validateConfirmPassword() {
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;
        
        clearFieldError(confirmPasswordInput);
        
        if (confirmPassword === '') {
            setFieldState(confirmPasswordInput, 'normal');
            return true;
        } else if (password === confirmPassword) {
            setFieldState(confirmPasswordInput, 'success');
            return true;
        } else {
            setFieldState(confirmPasswordInput, 'error');
            showFieldError(confirmPasswordInput, '两次输入的密码不一致');
            return false;
        }
    }

    // 计算密码强度
    function calculatePasswordStrength(password) {
        let strength = 0;
        
        if (password.length >= 6) strength++;
        if (password.match(/[a-z]/)) strength++;
        if (password.match(/[A-Z]/)) strength++;
        if (password.match(/[0-9]/)) strength++;
        if (password.match(/[^a-zA-Z0-9]/)) strength++;
        
        return strength;
    }

    // 更新密码强度显示
    function updatePasswordStrength(strength) {
        strengthBar.className = 'strength-bar';
        
        if (strength === 0) {
            strengthBar.style.width = '0%';
        } else if (strength <= 2) {
            strengthBar.classList.add('weak');
        } else if (strength === 3) {
            strengthBar.classList.add('fair');
        } else if (strength === 4) {
            strengthBar.classList.add('good');
        } else {
            strengthBar.classList.add('strong');
        }
    }

    // 设置字段状态
    function setFieldState(field, state) {
        field.classList.remove('error', 'success');
        if (state === 'error') {
            field.classList.add('error');
        } else if (state === 'success') {
            field.classList.add('success');
        }
    }

    // 显示字段错误
    function showFieldError(field, message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.textContent = message;
        
        const inputGroup = field.closest('.input-group');
        inputGroup.appendChild(errorDiv);
    }

    // 清除字段错误
    function clearFieldError(field) {
        const inputGroup = field.closest('.input-group');
        const errorMsg = inputGroup.querySelector('.error-message');
        if (errorMsg) {
            errorMsg.remove();
        }
    }

    // 验证所有字段
    function validateAllFields() {
        const usernameValid = usernameInput.value.trim() && validateUsername();
        const nicknameValid = nicknameInput.value.trim() && validateNickname();
        const passwordValid = passwordInput.value && validatePassword();
        const confirmPasswordValid = confirmPasswordInput.value && validateConfirmPassword();
        
        return usernameValid && nicknameValid && passwordValid && confirmPasswordValid;
    }

    // 提交表单
    function submitForm() {
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
        
        // 模拟提交延迟
        setTimeout(() => {
            form.submit();
        }, 500);
    }
}); 