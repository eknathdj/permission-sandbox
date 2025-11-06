package com.eknathdj.permissionsandbox

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var tvStatus: TextView
    private lateinit var btnCreate: Button

    private val provisionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Provisioning started/completed", Toast.LENGTH_LONG).show()
            tvStatus.text = "Sandbox status: provisioned (check app drawer for briefcase/badged icons)"
        } else {
            Toast.makeText(this, "Provisioning canceled or failed", Toast.LENGTH_LONG).show()
            tvStatus.text = "Sandbox status: not provisioned"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, SandboxDeviceAdminReceiver::class.java)

        tvStatus = findViewById(R.id.tvStatus)
        btnCreate = findViewById(R.id.btnCreateSandbox)

        btnCreate.setOnClickListener { startProvisioningFlowIfAllowed() }
    }

    private fun startProvisioningFlowIfAllowed() {
        val action = DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE
        val allowed = dpm.isProvisioningAllowed(action)
        if (!allowed) {
            Toast.makeText(this, "This device does not allow managed profile provisioning", Toast.LENGTH_LONG).show()
            tvStatus.text = "Sandbox status: provisioning NOT allowed"
            return
        }

        val intent = Intent(action).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                adminComponent
            )
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEFAULT_MANAGED_PROFILE_NAME,
                "Sandbox"
            )
        }
        provisionLauncher.launch(intent)
    }
}
