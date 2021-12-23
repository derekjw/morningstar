import com.derekjw.morningstar.*
import zio.*
import zio.Console.printLine
import zio.Clock
import zio.ZLogger

import javax.sound.midi.{MidiDevice, MidiSystem}
import izumi.reflect.Tag

object Main extends ZIOApp:

  type Environment = ZEnv & Morningstar

  val layer: RLayer[ZIOAppArgs, Environment] =
    ZEnv.live >+> (Midi.live(_.getName == "WIDI Bud Pro") >>> Morningstar.live(channel = 0))

  implicit val tag: Tag[Environment] = Tag[Environment]

  val run: URIO[Morningstar, ExitCode] =
    val app = for
      _ <- Morningstar.bankUp
      _ <- ZIO.logInfo("Done")
    yield ()

    app.orDie.exitCode

