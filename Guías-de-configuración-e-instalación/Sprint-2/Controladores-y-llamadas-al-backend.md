### AuthController

Subdominio: 
`/users/auth`

Endpoints:

```
    @PostMapping("/login")
```

```
    @PostMapping("/logout")
```

```
    @PostMapping("/privileged-login")
```

```
    @PostMapping("/step1")
```

```
    @PostMapping("/step2")
```

```
    @PostMapping("/step3")
```

### ContentController

Subdominio:
`/users/manage`

Endpoints:
```
	@PutMapping("/validate-creator/{creatorId}")
```

```
	@GetMapping("/all-users")
```


### TwoFactorAuthController

Subdominio
`/auth/2fa`

Endpoints:

```
    @PostMapping("/email/send")
```

```
    @PostMapping("/email/verify")
```

```
    @PostMapping("/totp/setup")
```

```
    @PostMapping("/totp/confirm")
```

```
    @PostMapping("/totp/verify")
```

```
    @PostMapping("/backup/verify")
```

```
    @PostMapping("/disable")
```

### RegistrationController

Subdominio:
`/users/register`

Endpoints:

```
    @PostMapping("/standard")
```

```
    @PostMapping("/creator")
```

```
    @PostMapping("/admin")
```

```
    @GetMapping("/confirm/{tokenId}")
```

El token que se envía desde el correo tiene el endpoint de `users/confirm`, pero realmente es `users/register/confirm/{tokenId}`
### PasswordController

Subdominio: 
`users/password`

```
    @PostMapping("/forgot")
```

```
    @PostMapping("/forgot-privileged")
```

```
    @PostMapping("/reset")
```

```
    @GetMapping("/validate-reset-token/{token}")
```

### ManagementController

Subdominio:
`/management`

Endpoints:
```
    @GetMapping("/user/{userConsultId}")
```

```
    @GetMapping("/creator/{creatorIdConsult}")
```

```
    @GetMapping("/admin/{adminIdConsult}")
```

```
    @PutMapping("/profile/{userId}")
```

```
    @PutMapping("/user/{userId}/password")
```

```
    @PutMapping("/creator/{creatorId}/password")
```

```
    @PutMapping("/admin/{adminId}/password")
```

```
    @PutMapping("/admin/user/{userId}")
```

```
    @PutMapping("/admin/user/{userId}/block")
```

```
    @PutMapping("/admin/creator/{creatorId}/block")
```

```
    @PutMapping("/creator/profile/{creatorId}")
```

```
    @PutMapping("/admin/profile")
```

```
    @PutMapping("/admin/creator/{creatorId}")
```

```
    @PutMapping("/admin/admin/{adminId}")
```

```
    @PutMapping("/validate-creator/{creatorId}")
```

```
    @DeleteMapping("/admin/delete/creator/{creatorId}")
```

```
    @DeleteMapping("/admin/delete/admin/{adminId}")
```

```
    @DeleteMapping("/creator/delete/self")
```

```
    @DeleteMapping("/user/delete/self")
```

```
    @PutMapping("/users/my-vip-status")
```

```
    @GetMapping("/users/my-vip-status")
```


### ContentController

Subdominio:
`/content`

Endpoints:

```
    @PostMapping("/upload-audio")
```

```
    @PostMapping("/upload-video")
```

```
    @GetMapping("/getAudio/{id}")
```

```
    @GetMapping("/getVideo/{id}")
```

```
    @GetMapping("/getAllContent")
```

```
    @GetMapping("/getAllAudios")
```

```
    @GetMapping("/getAllVideos")
```

```
    @PutMapping("/update-audio/{id}")
```

```
    @PutMapping("/update-video/{id}")
```

```
    @PostMapping("/rate/{idContenido}")
```

```
    @PostMapping("/increment-views-audio/{id}")
```

```
    @PostMapping("/increment-views-video/{id}")
```

```
    @GetMapping("/statistics/rankings")
```