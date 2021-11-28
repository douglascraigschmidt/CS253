package admin

import edu.vanderbilt.imagecrawler.utils.Assignment
import edu.vanderbilt.imagecrawler.utils.Assignment.isAssignment
import edu.vanderbilt.imagecrawler.utils.Student
import edu.vanderbilt.imagecrawler.utils.Student.Type.Graduate
import edu.vanderbilt.imagecrawler.utils.Student.Type.Undergraduate
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import org.junit.rules.Timeout.seconds
import org.junit.runners.model.Statement
import org.mockito.junit.MockitoJUnit
import org.mockito.stubbing.LenientStubber
import org.mockito.stubbing.OngoingStubbing
import kotlin.test.fail

/**
 * Base class used for all assignment test classes
 */
open class AssignmentTests(
    timeoutSeconds: Int = System.getenv().get("ASSIGNMENT_TESTS_TIMEOUT")?.toInt() ?: 10) {

    /** Required for all mockito tests */
    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    @Rule
    @JvmField
    var mockkRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                MockKAnnotations.init(
                    this,
                    relaxUnitFun = true,
                    overrideRecordPrivateCalls = true
                )
                try {
                    base?.evaluate()
                } finally {
                    //TODOx: is this necessary?
                    //unmockkAll()
                }
            }
        }
    }


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    /**
     * Sets all tests to timeout after 5 seconds.
     */
    @Rule
    @JvmField
    var timeout: Timeout = seconds(timeoutSeconds.toLong())

    /** Kotlin mocking library is missing this whenever extension. */
    fun <T> LenientStubber.whenever(methodCall: T): OngoingStubbing<T> {
        return `when`(methodCall)!!
    }

    /**
     * Throws [org.junit.AssumptionViolatedException]
     * project is does not match the [assignment].
     */
    fun assignmentTest(assignment: Assignment.Name) {
        assumeTrue("Assignment $assignment test ignored.", isAssignment(assignment))
    }

    /**
     * This call the a side-effect of setting the Assignment.sTypes
     * member to type iff (Assignment.sTypes & type) == true.
     */
    fun runAs(vararg args: Any): Boolean {
        check(args.size in 1..2) {
            "runAs() should have 1 or 2 parameters (not $args.size)"
        }
        check(args.size < 2 || args[0] != args[1]) {
            "runAs() should have 2 different parameters."
        }

        mockkStatic(Assignment::class, Student::class)

        args.forEach { arg ->
            when (arg) {
                is Assignment.Name -> {
                    // Throws an assumption exception if the passed assignment is not
                    // part of the current project.
                    assumeTrue("$arg test ignored.", Assignment.includes(arg))
                    every { Assignment.includes(any()) } answers {
//                        val result = call.invocation.args[0] == arg
//                        println("Assignment is ${call.invocation.args[0]}? answer: $result (mocking $arg)")
                        call.invocation.args[0] == arg
                    }
                }
                is Student.Type -> {
                    check(arg == Graduate || arg == Undergraduate) {
                        "runAs Int value must be $Graduate or $Undergraduate"
                    }
                    // Throws an assumption exception if the passed student type not
                    // included in the set of student types supported in this project.
                    assumeTrue("$arg test ignored.", Student.`is`(arg))
                    every { Student.`is`(any()) } answers {
//                        val result = call.invocation.args[0] == arg
//                        println("Student is ${call.invocation.args[0]}? answer: $result (mocking $arg)")
                        call.invocation.args[0] == arg
                    }
                }
                else -> error("Invalid runAs type $arg.")
            }
        }

        return true
    }

    fun verifyOneOf(message: String, vararg block: () -> Unit) {
        val status = mutableListOf<Boolean>()
        block.forEach {
            status.add(
                try {
                    it.invoke()
                    true
                } catch (t: Throwable) {
                    false
                }
            )
        }

        if (status.count { it } != 1) {
            fail(message)
        }
    }
}
