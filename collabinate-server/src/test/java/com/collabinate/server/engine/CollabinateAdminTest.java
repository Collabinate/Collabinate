package com.collabinate.server.engine;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.collabinate.server.Tenant;

/**
 * Abstract test class to test any implementation of a CollabinateAdmin.
 * 
 * @author mafuba
 *
 */
public abstract class CollabinateAdminTest
{
	private CollabinateAdmin admin;
	
	abstract CollabinateAdmin getAdmin();
	
	abstract CollabinateAdmin getNewAdmin();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup()
	{
		admin = getAdmin();
	}
	
	@Test
	public void get_nonexistent_tenant_should_return_null()
	{
		assertNull(admin.getTenant("tenant"));
	}
	
	@Test
	public void get_tenant_should_return_correct_tenant()
	{
		admin.putTenant(new Tenant("tenant", "tenantname"));		
		Tenant tenant = admin.getTenant("tenant");
		assertEquals("tenant", tenant.getId());
		assertEquals("tenantname", tenant.getName());
	}
	
	@Test
	public void putting_existing_tenant_should_modify_existing_tenant()
	{
		Tenant tenant1 = new Tenant("tenant", "tenant1");
		Tenant tenant2 = new Tenant("tenant", "tenant2");
		admin.putTenant(tenant1);
		admin.putTenant(tenant2);
		Tenant retrieved = admin.getTenant("tenant");
		assertEquals(tenant2.getName(), retrieved.getName());
	}
	
	@Test
	public void get_deleted_tenant_should_return_null()
	{
		admin.putTenant(new Tenant("tenant", "tenant"));
		admin.deleteTenant("tenant");
		
		assertNull(admin.getTenant("tenant"));
	}
	
	@Test
	public void delete_tenant_should_not_affect_other_tenants()
	{
		admin.putTenant(new Tenant("tenant1", "Tenant 1"));
		admin.putTenant(new Tenant("tenant2", "Tenant 2"));
		admin.deleteTenant("tenant1");
		
		assertNotNull(admin.getTenant("tenant2"));
	}

	@Test
	public void database_export_should_contain_added_tenant_name()
	{
		Tenant tenant = new Tenant("tenant", "testtenant");
		admin.putTenant(tenant);
		String backup = admin.exportDatabase();
		
		assertThat(backup, containsString(tenant.getName()));
	}
	
	@Test
	public void database_import_should_restore_existing_tenant()
	{
		admin.putTenant(new Tenant("tenant", "Tenant Backup"));
		String data = admin.exportDatabase();
		admin = getNewAdmin();
		
		assertNull(admin.getTenant("tenant"));
		
		admin.importDatabase(data);
		
		assertEquals(admin.getTenant("tenant").getName(), "Tenant Backup");
	}
	
	@Test
	public void tenant_export_should_contain_tenant_name()
	{
		Tenant tenant = new Tenant("tenant", "Export Tenant");
		admin.putTenant(tenant);
		String tenantBackup = admin.exportTenantData(tenant.getId());
		
		assertThat(tenantBackup, containsString(tenant.getName()));
	}
	
	@Test
	public void tenant_export_should_not_contain_other_tenant_names()
	{
		Tenant tenant1 = new Tenant("tenant1", "Export Tenant");
		Tenant tenant2 = new Tenant("tenant2", "Other Tenant");
		admin.putTenant(tenant1);
		admin.putTenant(tenant2);
		String tenantBackup = admin.exportTenantData(tenant1.getId());
		
		assertThat(tenantBackup, not(containsString(tenant2.getName())));
	}
	
	@Test
	public void tenant_import_should_fail_for_existing_tenant()
	{
		exception.expect(IllegalStateException.class);
		exception.expectMessage("tenant");
		
		Tenant tenant = new Tenant("tenant", "Tenant");
		admin.putTenant(tenant);
		String tenantBackup = admin.exportTenantData(tenant.getId());
		
		admin.importTenantData(tenant.getId(), tenantBackup);
	}
	
	@Test
	public void tenant_import_should_restore_tenant_in_new_database()
	{
		Tenant tenant = new Tenant("tenant", "Restored Tenant");
		admin.putTenant(tenant);
		String tenantBackup = admin.exportTenantData(tenant.getId());
		admin = getNewAdmin();
		admin.importTenantData(tenant.getId(), tenantBackup);
		Tenant restored = admin.getTenant(tenant.getId());
		
		assertEquals(tenant.getName(), restored.getName());
	}
	
	@Test
	public void tenant_import_should_restore_tenant_in_existing_database()
	{
		Tenant tenant1 = new Tenant("tenant1", "Restored Tenant");
		Tenant tenant2 = new Tenant("tenant2", "Static Tenant");
		admin.putTenant(tenant1);
		admin.putTenant(tenant2);
		String tenantBackup = admin.exportTenantData(tenant1.getId());
		admin.deleteTenant(tenant1.getId());
		admin.importTenantData(tenant1.getId(), tenantBackup);
		Tenant restored = admin.getTenant(tenant1.getId());
		
		assertEquals(tenant1.getName(), restored.getName());		
	}
	
	@Test
	public void tenant_import_should_not_disturb_other_tenants()
	{
		Tenant tenant1 = new Tenant("tenant1", "Restored Tenant");
		Tenant tenant2 = new Tenant("tenant2", "Static Tenant");
		admin.putTenant(tenant1);
		admin.putTenant(tenant2);
		String tenantBackup = admin.exportTenantData(tenant1.getId());
		admin.deleteTenant(tenant1.getId());
		admin.importTenantData(tenant1.getId(), tenantBackup);
		Tenant existing = admin.getTenant(tenant2.getId());
		
		assertEquals(tenant2.getName(), existing.getName());		
	}
}
