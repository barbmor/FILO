package pl.opole.uni.cs.unifDL.Filo.model;

public class DecompositionVariable extends Variable {

	final Integer parentId;
	final Integer roleid;

	public DecompositionVariable(Integer parentId, Integer roleId, Integer conceptNameId) {
		super(conceptNameId);
		this.parentId = parentId;
		this.roleid = roleId;
	}

	public Integer getParent() {
		return this.parentId;
	}

	public Integer getRole() {
		return roleid;
	}
}