package processor

import java.io.IOException

import cats.effect.{ ContextShift, IO }
import cats.implicits._
import model.{ FailedAggregation, OutputData, ValidAggregation }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext

class CsvProcessorSpec extends AnyFlatSpec with should.Matchers {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private val directoryName = getClass.getResource("/default").getPath

  val defaultOutput: OutputData = OutputData(
    HashMap(
      "s1" -> ValidAggregation(10, 98, 108, 2),
      "s2" -> ValidAggregation(78, 88, 246, 3),
      "s3" -> FailedAggregation
    ),
    2,
    7,
    2
  )

  trait Processor {
    val processor: CsvProcessor = new ParallelAkkaProcessor()
  }

  "Output of default test" should "be as specified in task.md including string representation" in new Processor {
    private val outputData = processor.run(directoryName).unsafeRunSync()
    outputData should be(defaultOutput)

    private def fixNewLine(s: String) = s.replaceAll("\\r\\n|\\r|\\n", "\\n")

    fixNewLine(outputData.toString) should be(fixNewLine("""Num of processed files: 2
        |Num of processed measurements: 7
        |Num of failed measurements: 2
        |
        |Sensors with highest avg humidity:
        |
        |sensor-id,min,avg,max
        |s2,78,82,88
        |s1,10,54,98
        |s3,NaN,NaN,NaN""".stripMargin))

  }

  it should "throw IOException for an invalid path" in new Processor {

    assertThrows[IOException] {
      val directoryName = getClass.getResource("/invalid").getPath
      processor.run(directoryName).unsafeRunSync()
    }

  }

}
