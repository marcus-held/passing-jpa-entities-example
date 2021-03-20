package de.held.passingjpaentities

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
class PassingjpaentitiesApplication

fun main(args: Array<String>) {
    runApplication<PassingjpaentitiesApplication>(*args)
}

@Entity
class User(
    @Id var id: UUID = UUID.randomUUID(),
    var username: String
)

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): User?
}

@Service
class UpdateUsernameService(private val userRepository: UserRepository) {

    @Transactional
    fun updateUsername(user: User, username: String) {
        user.username = username
    }

    @Transactional
    fun updateUsernameById(userId: UUID, username: String) {
        val user = userRepository.findByIdOrNull(userId) ?: throw IllegalStateException()
        user.username = username
    }

}

@Service
class BanUserService(
    private val userRepository: UserRepository,
    private val updateUsernameService: UpdateUsernameService
) {

    @Transactional
    fun banUser(username: String) {
        val user = userRepository.findByUsername(username) ?: throw IllegalStateException() // We do a select on the database
        updateUsernameService.updateUsernameById(user.id, "Banned")
        // do some other operations
    }

}
