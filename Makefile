setup:
	@if ! command -v allure >/dev/null 2>&1; then \
	  if command -v brew >/dev/null 2>&1; then \
	    brew install allure; \
	  fi; \
	  if command -v apt-get >/dev/null 2>&1; then \
	    curl -o allure-2.13.8.tgz -OLs https://repo.maven.apache.org/maven2/io/qameta/allure/allure-commandline/2.13.8/allure-commandline-2.13.8.tgz && \
	    tar -zxvf allure-2.13.8.tgz -C /opt/ && \
	    ln -s /opt/allure-2.13.8/bin/allure /usr/bin/allure; \
	   else \
	      echo "You have to install https://github.com/icefox/git-hooks"; \
	    exit 1; \
	  fi; \
	fi

build:
	./gradlew build

demo-run:
	./scripts/run.sh /Users/kannanr/Downloads/jsonbox_test.xlsx

lint:
	./gradlew spotlessJavaApply
