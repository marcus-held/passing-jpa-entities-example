package de.held.passingjpaentities

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class PassingjpaentitiesApplicationTests {

    private val log = getLogger(this.javaClass)

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var updateUsernameService: UpdateUsernameService

    @Autowired
    private lateinit var banUserService: BanUserService

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    private val id = UUID.randomUUID()

    private val username = "test"

    @BeforeAll
    fun setup() {
        userRepository.save(User(id, username))
    }

    @Nested
    inner class UpdateUsername {
        @Test
        fun `user is not updated when passing a detached entity`() {
            log.info("Find user")
            val user = userRepository.findByIdOrNull(id)!!
            // user is in a detached state since we are outside of a persistence context
            log.info("Call update username")
            updateUsernameService.updateUsername(user, "updated")

            log.info("Assert")
            Assertions.assertThat(userRepository.findByIdOrNull(id)!!.username)
                .isNotEqualTo("updated")
        }

        @Test
        fun `user is updated inside of a persistent context`() {
            transactionTemplate.execute {
                log.info("Find user")
                val user = userRepository.findByIdOrNull(id)!!
                // This time the user is inside a persistent context and JPA takes care to persist it
                log.info("Call update username")
                updateUsernameService.updateUsername(user, "updated")
            }

            log.info("Assert")
            Assertions.assertThat(userRepository.findByIdOrNull(id)!!.username)
                .isEqualTo("updated")
        }
    }

    @Nested
    inner class BanUser {

        @Test
        fun `Call banUser to check for multiple selects`() {
            banUserService.banUser(username)
            // I don't assert here, since it's unhandy to figure out which statements were executed
        }

    }

    @Nested
    inner class MandatoryTransaction {

        @Test
        fun `Calling update username without persistent context throws an exception`() {
            val user = userRepository.findByIdOrNull(id)!!
            assertThrows<Exception> { updateUsernameService.updateUsernameWithMandatoryTransaction(user, "updated") }
        }

        @Test
        fun `Calling update username with transaction updates the user`() {
            transactionTemplate.execute {
                val user = userRepository.findByIdOrNull(id)!!
                updateUsernameService.updateUsernameWithMandatoryTransaction(user, "updated")
            }

            Assertions.assertThat(userRepository.findByIdOrNull(id)!!.username)
                .isEqualTo("updated")

        }

    }



}
