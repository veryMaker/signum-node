package brs

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Arguments(parser: ArgParser) {
    val configDirectory by parser.storing(
        "--config", "-c",
        help = "The configuration directory where the properties files are stored"
    ).default("conf/")

    val headless by parser.flagging(
        "--headless",
        help = "Runs in headless mode - with the GUI extension disabled"
    ).default(false)

    companion object {
        fun parse(args: Array<String>): Arguments = ArgParser(args).parseInto(::Arguments)
    }
}
