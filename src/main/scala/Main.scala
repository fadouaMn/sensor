import cats.effect._
import cats.implicits._
import processor.{ CsvProcessor, ParallelAkkaProcessor }

object Main extends IOApp {

  /** Entry point.
    *
    * @param args
    *   command line arguments args(0) - optional input directory name args(1)
    * @return
    *   exit code
    */
  override def run(args: List[String]): IO[ExitCode] = {
    val ioExit = for {
      csvProcessor <- IO(new ParallelAkkaProcessor())
      result       <- csvProcessor.run(args.headOption.getOrElse("."))
      _            <- IO(println(result))
    } yield ExitCode.Success

    ioExit.handleError(throwable => {
      println(throwable)
      ExitCode.Error
    })
  }

}
