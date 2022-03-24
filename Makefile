setup:
	@if ! command -v allure >/dev/null 2>&1; then \
	  if command -v brew >/dev/null 2>&1; then \
	    brew install allure; \
	  fi; \
	  if command -v apt-get >/dev/null 2>&1; then \
	     sudo apt-add-repository ppa:qameta/allure &&  sudo apt-get update -y &&  sudo apt-get install -y allure; \
	   else \
	      echo "You have to install https://github.com/icefox/git-hooks"; \
	    exit 1; \
	  fi; \
	fi

build:
	./gradlew build

run: $(OUT)
	./$(OUT)

demo-run:
	./scripts/run.sh quickstart/jsonbox_test.xlsx