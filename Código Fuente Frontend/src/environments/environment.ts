
export const environment = {
  production: false,
  
  //Cambiar apiBaseURL a true para deploy, a false para desarrollo local
  apiBaseUrl: true ? 'https://g07-esimedia-pi-backend.onrender.com' : 'http://localhost:9090',
  
  // Endpoints de usuarios
    endpoints: {

    users: {
      auth:{
        login: '/users/auth/login', 
        logout: '/users/auth/logout', 
        privilegedLogin: '/users/auth/privileged-login', //SI

        auth2fa:{
          sendEmail:'/auth/2fa/email/send', //SI
          verifyEmail:'/auth/2fa/email/verify', //SI
          generateQR: '/auth/2fa/totp/setup', //SI
          confirmTOTP: '/auth/2fa/totp/confirm', //SI
          verifyTOTP: '/auth/2fa/totp/verify', //SI
          disable2FA: '/auth/2fa/disable', //SI
          disable3FA: '/auth/2fa/totp/disable', //SI
          sendEmailAdmin:'/auth/2fa/admin/email/send', //SI
          verifyEmailAdmin:'/auth/2fa/admin/email/verify', //SI
          generateQRAdmin: '/auth/2fa/admin/totp/setup', //SI
          confirmTOTPAdmin: '/auth/2fa/admin/totp/confirm', //SI
          verifyTOTPAdmin: '/auth/2fa/admin/totp/verify', //SI
          sendEmailCreator:'/auth/2fa/creator/email/send', //SI
          verifyEmailCreator:'/auth/2fa/creator/email/verify', //SI
          generateQRCreator: '/auth/2fa/creator/totp/setup', //SI
          confirmTOTPCreator: '/auth/2fa/creator/totp/confirm', //SI
          verifyTOTPCreator: '/auth/2fa/creator/totp/verify', //SI
        }
      },
      password:{
        forgot: '/users/password/forgot', //SI
        forgotPrivileged: '/users/password/forgot-privileged', //SI
        reset: '/users/password/reset', //SI
        validateResetToken: '/users/password/validate-reset-token/{token}', 
      },
      register:{
        standard: '/users/register/standard', //SI
        creator: '/users/register/creator', //SI
        admin: '/users/register/admin', //SI
        confirmToken: '/confirm/{tokenID}' 
      },
      manage:{
        validateCreator: '/users/manage/validate-creator/{creatorId}', 
        getAllUsers: '/users/manage/all-users' //SI
      }
    },

    management: {
      user:{
        getUser: '/management/user/{userId}', //SI
        updateProfile: '/management/profile/{userId}', //SI
        toggleVipStatus: '/management/users/my-vip-status', //SI
        changePassword: '/management/user/{userId}/password', //SI
        selfDelete: '/management/user/delete/self', //SI
      },
      creator:{
        getCreator: '/management/creator/{creatorId}', //SI
        updateProfile: '/management/creator/profile/{creatorId}', //SI
        changePassword: '/management/creator/{creatorId}/password', //SI
        selfDelete: '/management/creator/delete/self', //SI
      },
      admin:{
        getAdmin: '/management/admin/{adminId}', //SI
        updateProfile: '/management/admin/profile', //SI
        changePassword: '/management/admin/{adminId}/password', //SI
        updateUser: '/management/admin/user/{userId}', //SI
        updateCreator: '/management/admin/creator/{creatorId}', //SI
        updateAdmin: '/management/admin/admin/{adminId}', //SI
        toggleUserBlock: '/management/admin/user/{userId}/block', //SI
        toggleCreatorBlock: '/management/admin/creator/{creatorId}/block', //SI
        validateCreator: '/management/validate-creator/{creatorId}', //SI
        deleteCreator: '/management/admin/delete/creator/{creatorId}', //SI
        deleteAdmin: '/management/admin/delete/admin/{adminId}', //SI
      },
    },

    
    
    // Endpoints de contenido
    content: {
      uploadAudio: '/content/upload-audio', //SI
      uploadVideo: '/content/upload-video', //SI
      getAudio: '/content/getAudio', //SI
      getVideo: '/content/getVideo', //SI
      getAllContent: '/content/getAllContent', //SI
      getAllAudios: '/content/getAllAudios', //SI
      getAllVideos: '/content/getAllVideos', //SI
      updateVideo: '/content/update-video/{id}', //SI
      updateAudio: '/content/update-audio/{id}', //SI
      deleteAudio: '/content/delete-audio/{id}', //SI
      deleteVideo: '/content/delete-video/{id}', //SI
      rateContent: '/content/rate/{id}', //SI
      viewedAudio: '/content/increment-views-audio/{id}', //SI
      viewedVideo: '/content/increment-views-video/{id}', //SI

      publicLists:{
        create: '/content/lists/public', //SI
        get: '/content/lists/public/contents', // SI
        edit: '/content/lists/public/fields', //SI
        addContent: '/content/lists/public/add-content', //SI 
        removeContent: '/content/lists/public/remove-content', //SI 
        deleteList: '/content/lists/public/{idLista}' //SI
      },
      privateLists:{
        create: '/content/lists/private', //SI
        get: '/content/lists/private/contents',// SI
        edit: '/content/lists/private/fields', //SI
        addContent: '/content/lists/private/add-content', //SI
        removeContent: '/content/lists/private/remove-content', //SI 
        deleteList: '/content/lists/private/{idLista}' //SI
      },
      favoritos:{
        add: '/favoritos/add', //SI 
        remove: '/favoritos/remove', //SI
        getAll: '/favoritos/mis-favoritos' //SI
      }
    },
    
    // Endpoints de tokens (si se necesitan en el futuro)
    tokens: {
      base: '/tokens'
    }
  }
};