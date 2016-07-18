package br.ufrn.deliverydelay.repositories.code;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.ufrn.deliverydelay.exceptions.MissingParameterException;
import br.ufrn.deliverydelay.model.ChangedPath;
import br.ufrn.deliverydelay.model.Commit;
import br.ufrn.deliverydelay.repositories.AbstractRepository;

public abstract class AbstractCodeRepository extends AbstractRepository implements CodeRepository {	

	protected Integer system;
	
	public AbstractCodeRepository(String username, String password, String url) {
		 super(username, password, url);
	}

	public boolean connect() throws MissingParameterException {
		if (url == null) {
			throw new MissingParameterException("Missing mandatory parameter: String url");
		}
		if (username == null) {
			throw new MissingParameterException("Missing mandatory parameter: String username");
		}
		if (password == null) {
			throw new MissingParameterException("Missing mandatory parameter: String password");
		}
		return specificConnect();
	}

	protected abstract boolean specificConnect();

	protected List<ChangedPath> findJavaChangedPaths(Matcher matcher, Commit commit) {
		List<ChangedPath> changedPaths = new LinkedList<ChangedPath>();
		while (matcher.find()) {
			String c1 = matcher.group(1);
			String trunk = "/" + matcher.group(3);
			String path = matcher.group(4);
			if (path.contains(".java")) {
				ChangedPath changedPath = new ChangedPath(trunk + path, c1.charAt(0), commit, null);
				changedPaths.add(changedPath);
			} else {
				continue;
			}
		}
		return changedPaths;
	}

	protected boolean isRemoval(String line) {
		Pattern pattern = Pattern.compile("-\\s.+");
		Matcher matcher = pattern.matcher(line);

		return matcher.find();
	}

	protected boolean verifyWhiteSpace(String line) {
		return line.replaceAll("\\+", "").replaceAll("-", "").trim().length() == 0;
	}

	public Integer getSystem() {
		return system;
	}

	public void setSystem(Integer system) {
		this.system = system;
	}
	
	protected boolean isAcceptedChangedPath(String path, List<String> acceptedPaths){
		for(String acceptedPath: acceptedPaths)
			if(path.contains(acceptedPath))
				return true;
		return false;
	}
}
