package com.esimedia.features.user_management.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.esimedia.features.auth.entity.Administrador;
import com.esimedia.features.auth.entity.CreadorContenido;
import com.esimedia.features.auth.entity.Usuario;
import com.esimedia.features.auth.entity.UsuarioNormal;
import com.esimedia.features.auth.repository.AdminRepository;
import com.esimedia.features.auth.repository.CreadorContenidoRepository;
import com.esimedia.features.auth.repository.UsuarioNormalRepository;

@Service
public class UserQueryService {

    private final UsuarioNormalRepository usuarioNormalRepository;
    private final CreadorContenidoRepository creadorContenidoRepository;
    private final AdminRepository adminRepository;
    private final Logger logger;

    // Constantes para mensajes de error y log
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private static final String CREADOR_NO_ENCONTRADO = "Creador no encontrado";
    private static final String USUARIO_NORMAL_ACTUALIZADO = "Usuario normal actualizado: {}";
    private static final String CREADOR_ACTUALIZADO = "Creador actualizado: {}";
    private static final String ADMIN_ACTUALIZADO = "Admin actualizado: {}";
    private static final String CREADOR_VALIDADO_EXITOSAMENTE = "Creador validado exitosamente";

    public UserQueryService(UsuarioNormalRepository usuarioNormalRepository,
                            CreadorContenidoRepository creadorContenidoRepository,
                            AdminRepository adminRepository) {
        this.usuarioNormalRepository = usuarioNormalRepository;
        this.creadorContenidoRepository = creadorContenidoRepository;
        this.adminRepository = adminRepository;
        this.logger = LoggerFactory.getLogger(UserQueryService.class);
    }

    public UsuarioNormal findUsuarioNormalByEmail(String email) {
        return usuarioNormalRepository.findByemail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
    }

    public CreadorContenido findCreadorByEmail(String email) {
        return creadorContenidoRepository.findByemail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO));
    }

    public Administrador findAdminByEmail(String email) {
        return adminRepository.findByemail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO));
    }

    public Optional<UsuarioNormal> findUsuarioNormalById(String id) {
        return usuarioNormalRepository.findById(id);
    }

    public Optional<CreadorContenido> findCreadorById(String id) {
        return creadorContenidoRepository.findById(id);
    }

    public Optional<Administrador> findAdminById(String id) {
        return adminRepository.findById(id);
    }

    public Object findAnyUserById(String id) {
        Optional<UsuarioNormal> normal = usuarioNormalRepository.findById(id);
        Optional<CreadorContenido> creador = creadorContenidoRepository.findById(id);
        Optional<Administrador> admin = adminRepository.findById(id);

        Object result = null;
        if (normal.isPresent()) {
            result = normal.get();
        } 
		else if (creador.isPresent()) {
            result = creador.get();
        } 
		else if (admin.isPresent()) {
            result = admin.get();
        }

        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO);
        }

        return result;
    }

    public void updateUsuarioNormal(UsuarioNormal user) {
        usuarioNormalRepository.save(user);
        logger.info(USUARIO_NORMAL_ACTUALIZADO, user.getIdUsuario());
    }

    public void updateCreador(CreadorContenido creador) {
        creadorContenidoRepository.save(creador);
        logger.info(CREADOR_ACTUALIZADO, creador.getIdUsuario());
    }

    public void updateAdmin(Administrador admin) {
        adminRepository.save(admin);
        logger.info(ADMIN_ACTUALIZADO, admin.getIdUsuario());
    }

    public Optional<UsuarioNormal> findByEmail(String email) {
        return usuarioNormalRepository.findByemail(email);
    }

    public Optional<UsuarioNormal> findByAlias(String alias) {
        return usuarioNormalRepository.findByalias(alias);
    }

    public Optional<UsuarioNormal> findById(String id) {
        return usuarioNormalRepository.findById(id);
    }

    public Optional<Usuario> findAnyUserByAlias(String alias) {
        Optional<Usuario> result = usuarioNormalRepository.findByalias(alias).map(u -> (Usuario) u);
        if (!result.isPresent()) {
            result = creadorContenidoRepository.findByAlias(alias).map(u -> (Usuario) u);
        }
        if (!result.isPresent()) {
            result = adminRepository.findByalias(alias).map(u -> (Usuario) u);
        }
        return result;
    }


    public void deleteUser(String userId) {
        if (!usuarioNormalRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USUARIO_NO_ENCONTRADO);
        }
        usuarioNormalRepository.deleteById(userId);
    }

    public String validateCreator(String creatorId) {
        Optional<CreadorContenido> creatorOpt = creadorContenidoRepository.findById(creatorId);
        
        if (!creatorOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, CREADOR_NO_ENCONTRADO);
        }

        CreadorContenido creator = creatorOpt.get();
        creator.setValidado(true);
        creadorContenidoRepository.save(creator);

        return CREADOR_VALIDADO_EXITOSAMENTE;
    }
}