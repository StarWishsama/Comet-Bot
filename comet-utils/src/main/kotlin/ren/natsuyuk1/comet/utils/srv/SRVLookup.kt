package ren.natsuyuk1.comet.utils.srv

import java.util.*
import javax.naming.Context
import javax.naming.directory.Attributes
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext

object SRVLookup {
    /**
     * Execute a srv lookup by jndi
     *
     * @param address target address
     * @param serviceName SRV service name
     *
     * @return srv record (include host and port)
     */
    fun lookup(address: String, serviceName: String): Pair<String, Int>? {
        val env = Hashtable<String, String>().also {
            it[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.dns.DnsContextFactory"
            it["java.naming.provider.url"] = "dns:"
        }

        val ctx: DirContext = InitialDirContext(env)

        val attributes: Attributes = ctx.getAttributes("_$serviceName._tcp.$address", arrayOf("SRV"))
        val srvResult = attributes.get("SRV").get(0).toString().split(" ")

        if (srvResult.isEmpty()) {
            return null
        }

        val host = srvResult[3]
        val port = srvResult[2]

        return Pair(host.removeSuffix("."), port.toInt())
    }
}
